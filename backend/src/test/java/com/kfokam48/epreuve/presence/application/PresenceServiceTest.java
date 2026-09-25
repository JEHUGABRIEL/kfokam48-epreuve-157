package com.kfokam48.epreuve.presence.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;
import com.kfokam48.epreuve.presence.application.dto.AjouterPresenceFormateurRequest;
import com.kfokam48.epreuve.presence.application.dto.MarquerPresenceRequest;
import com.kfokam48.epreuve.presence.application.dto.PresenceResponse;
import com.kfokam48.epreuve.presence.domain.CodeExpireException;
import com.kfokam48.epreuve.presence.domain.CodeInconnuException;
import com.kfokam48.epreuve.presence.domain.DejaPresentException;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.TropDeTentativesException;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.presence.domain.model.SourcePresence;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;
import com.kfokam48.epreuve.session.domain.model.StatutSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Les règles de EF1, testées seules : ni base de données, ni contexte Spring, ni HTTP. C'est ce que
 * permet la séparation entre le modèle du domaine et l'entité de stockage.
 */
class PresenceServiceTest {

    private PresenceRepository presenceRepository;
    private SessionRepository sessionRepository;
    private EtudiantRepository etudiantRepository;
    private PresenceService presenceService;

    @BeforeEach
    void preparerLeService() {
        presenceRepository = mock(PresenceRepository.class);
        sessionRepository = mock(SessionRepository.class);
        etudiantRepository = mock(EtudiantRepository.class);
        // Le vrai compteur : RG8 est une règle en mémoire, la simuler testerait la simulation.
        presenceService = new PresenceService(presenceRepository, sessionRepository, etudiantRepository,
                new CompteurDeTentatives());
    }

    // RG11 / Q14 : le formateur ajoute la présence à la main, et l'ajout doit se VOIR.
    @Test
    void le_formateur_ajoute_une_presence_marquee_comme_telle() {
        given(sessionRepository.trouverParId(10L)).willReturn(Optional.of(sessionOuverte()));
        given(etudiantRepository.trouverParId(7L)).willReturn(Optional.of(new Etudiant()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L)).willReturn(Optional.empty());
        given(presenceRepository.enregistrer(any())).willAnswer(invocation -> invocation.getArgument(0));

        PresenceResponse reponse = presenceService.ajouterParFormateur(
                new AjouterPresenceFormateurRequest(10L, 7L));

        // La source est le seul moyen, pour le formateur, de distinguer une présence saisie d'un ajout
        // manuel — et c'est exactement ce que Q14 demande de rendre visible.
        assertThat(reponse.source()).isEqualTo(SourcePresence.FORMATEUR);
    }

    @Test
    void le_formateur_ne_duplique_pas_une_presence_existante() {
        given(sessionRepository.trouverParId(10L)).willReturn(Optional.of(sessionOuverte()));
        given(etudiantRepository.trouverParId(7L)).willReturn(Optional.of(new Etudiant()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L))
                .willReturn(Optional.of(new Presence()));

        assertThatThrownBy(() -> presenceService.ajouterParFormateur(
                new AjouterPresenceFormateurRequest(10L, 7L)))
                .isInstanceOf(DejaPresentException.class);
    }

    @Test
    void le_formateur_ne_rouvre_pas_une_session_cloturee() {
        Session cloturee = sessionOuverte();
        cloturee.setStatut(StatutSession.CLOTUREE);
        given(sessionRepository.trouverParId(10L)).willReturn(Optional.of(cloturee));

        assertThatThrownBy(() -> presenceService.ajouterParFormateur(
                new AjouterPresenceFormateurRequest(10L, 7L)))
                .isInstanceOf(SessionDejaClotureeException.class);
    }

    @Test
    void un_code_valide_enregistre_la_presence_en_source_etudiant() {
        given(sessionRepository.trouverParCode("ABC234")).willReturn(Optional.of(sessionOuverte()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L)).willReturn(Optional.empty());
        given(presenceRepository.enregistrer(any())).willAnswer(invocation -> {
            Presence aEnregistrer = invocation.getArgument(0);
            aEnregistrer.setId(1L);
            return aEnregistrer;
        });

        PresenceResponse reponse = presenceService.marquer(new MarquerPresenceRequest("ABC234", 7L));

        assertThat(reponse.id()).isEqualTo(1L);
        assertThat(reponse.sessionId()).isEqualTo(10L);
        assertThat(reponse.etudiantId()).isEqualTo(7L);
        assertThat(reponse.source()).isEqualTo(SourcePresence.ETUDIANT);
    }

    @Test
    void un_code_inconnu_est_refuse_en_400() {
        given(sessionRepository.trouverParCode("FAUX99")).willReturn(Optional.empty());

        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("FAUX99", 7L)))
                .isInstanceOf(CodeInconnuException.class);
    }

    // RG1 : 15 minutes après l'ouverture, le code ne marche plus — c'est la session qui le dit.
    @Test
    void un_code_expire_est_refuse_en_410() {
        given(sessionRepository.trouverParCode("VIEUX1")).willReturn(Optional.of(sessionExpiree()));

        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("VIEUX1", 7L)))
                .isInstanceOf(CodeExpireException.class);
    }

    // RG7 : une seule présence par étudiant et par session.
    @Test
    void une_seconde_presence_est_refusee_en_409() {
        given(sessionRepository.trouverParCode("ABC234")).willReturn(Optional.of(sessionOuverte()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L))
                .willReturn(Optional.of(new Presence()));

        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("ABC234", 7L)))
                .isInstanceOf(DejaPresentException.class);
    }

    // #78 — le cas signalé : deux saisies du même étudiant arrivent presque en même temps. La lecture
    // ci-dessus ne voit rien (la première n'est pas encore écrite), donc les deux requêtes tentent
    // l'insertion, et la contrainte d'unicité de la base rejette la seconde. C'est la base qui a raison
    // (ENF4), mais le contrat impose 409 DEJA_PRESENT : l'erreur de stockage doit être traduite, pas
    // laissée remonter en 500 ERREUR_INTERNE. Ce test échoue tant que la traduction n'existe pas.
    @Test
    void une_seconde_saisie_rejetee_par_la_base_sort_en_409_pas_en_500() {
        given(sessionRepository.trouverParCode("ABC234")).willReturn(Optional.of(sessionOuverte()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L)).willReturn(Optional.empty());
        given(presenceRepository.enregistrer(any())).willThrow(new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_presence_session_etudiant\""));

        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("ABC234", 7L)))
                .isInstanceOf(DejaPresentException.class);
    }

    // #78 — même porte, autre entrée : l'ajout manuel du formateur subit la même course.
    @Test
    void un_ajout_du_formateur_rejete_par_la_base_sort_en_409_pas_en_500() {
        given(sessionRepository.trouverParId(10L)).willReturn(Optional.of(sessionOuverte()));
        given(etudiantRepository.trouverParId(7L)).willReturn(Optional.of(new Etudiant()));
        given(presenceRepository.trouverParSessionEtEtudiant(10L, 7L)).willReturn(Optional.empty());
        given(presenceRepository.enregistrer(any())).willThrow(new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_presence_session_etudiant\""));

        assertThatThrownBy(() -> presenceService.ajouterParFormateur(
                new AjouterPresenceFormateurRequest(10L, 7L)))
                .isInstanceOf(DejaPresentException.class);
    }

    // RG13 : après clôture, plus rien n'est accepté — même avec un code encore valable.
    @Test
    void une_session_cloturee_refuse_la_presence_en_409() {
        Session cloturee = sessionOuverte();
        cloturee.setStatut(StatutSession.CLOTUREE);
        given(sessionRepository.trouverParCode("ABC234")).willReturn(Optional.of(cloturee));

        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("ABC234", 7L)))
                .isInstanceOf(SessionDejaClotureeException.class);
    }

    // RG8 : cinq échecs bloquent deux minutes. Le sixième essai est refusé AVANT toute lecture du
    // code, donc même un code valide ne passe plus : c'est ce qui empêche d'essayer les codes un à un.
    @Test
    void cinq_echecs_bloquent_deux_minutes_meme_avec_un_bon_code() {
        given(sessionRepository.trouverParCode("FAUX99")).willReturn(Optional.empty());
        for (int tentative = 0; tentative < 5; tentative++) {
            assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("FAUX99", 7L)))
                    .isInstanceOf(CodeInconnuException.class);
        }

        given(sessionRepository.trouverParCode("ABC234")).willReturn(Optional.of(sessionOuverte()));
        assertThatThrownBy(() -> presenceService.marquer(new MarquerPresenceRequest("ABC234", 7L)))
                .isInstanceOf(TropDeTentativesException.class);
    }

    private Session sessionOuverte() {
        Session session = new Session();
        session.setId(10L);
        session.setCode("ABC234");
        session.setOuvertureAt(Instant.now().minusSeconds(60));
        session.setExpirationAt(Instant.now().plusSeconds(14 * 60));
        return session;
    }

    private Session sessionExpiree() {
        Session session = new Session();
        session.setId(11L);
        session.setCode("VIEUX1");
        session.setOuvertureAt(Instant.now().minusSeconds(20 * 60));
        session.setExpirationAt(Instant.now().minusSeconds(5 * 60));
        return session;
    }
}
