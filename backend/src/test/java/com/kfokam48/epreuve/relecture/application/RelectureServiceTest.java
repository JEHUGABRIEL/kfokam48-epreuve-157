package com.kfokam48.epreuve.relecture.application;

import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;
import com.kfokam48.epreuve.exercice.domain.model.StatutExercice;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.presence.domain.model.SourcePresence;
import com.kfokam48.epreuve.relecture.application.dto.RendreRelectureRequest;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;
import com.kfokam48.epreuve.session.domain.model.StatutSession;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitaire (B6) des règles de gestion RG2, RG4 (révisée à l'étape 3) et RG5 — un étudiant ne
 * relit jamais son propre exercice, deux pairs distincts sont tirés parmi les présents, et l'exercice
 * attend la dernière note pour être clos.
 *
 * <p>Pas de contexte Spring : le tirage au sort est une interface, donc le test peut décider qui gagne
 * et vérifier qui était éligible — c'est ce qui rend « jamais l'auteur » et « jamais deux fois le
 * même » vérifiables plutôt que probables.
 */
class RelectureServiceTest {

    private final RelectureRepository relectures = mock(RelectureRepository.class);
    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final ExerciceRepository exercices = mock(ExerciceRepository.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final TirageAuSort tirage = mock(TirageAuSort.class);
    private final RelectureService service = new RelectureService(
            relectures, presences, exercices, sessions, tirage);

    @Test
    void lauteur_nest_jamais_dans_les_candidats() {
        // L'auteur (1) est présent, comme souvent : il ne doit pourtant jamais être tiré (RG2, Q5).
        presencesDeLaSession(1L, 2L, 3L);
        when(tirage.tirer(anyList())).thenReturn(3L, 2L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        service.assigner(10L, 1L, 1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> candidats = ArgumentCaptor.forClass(List.class);
        verify(tirage, times(2)).tirer(candidats.capture());
        assertEquals(List.of(2L, 3L), candidats.getAllValues().get(0), "l'auteur figure parmi les candidats");
        // Le second tirage part des candidats restants : le premier tiré n'y est plus (RG4 révisée).
        assertEquals(List.of(2L), candidats.getAllValues().get(1), "le premier tiré pouvait être tiré deux fois");
    }

    // RG4 révisée (étape 3) : deux pairs distincts, donc deux relectures, jamais la même personne.
    @Test
    void assigne_deux_pairs_distincts() {
        presencesDeLaSession(1L, 2L, 3L, 4L);
        when(tirage.tirer(anyList())).thenReturn(3L, 4L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        ArgumentCaptor<Relecture> enregistrees = ArgumentCaptor.forClass(Relecture.class);
        verify(relectures, times(2)).enregistrer(enregistrees.capture());
        List<Long> relecteurs = enregistrees.getAllValues().stream()
                .map(Relecture::getRelecteurId)
                .toList();
        assertEquals(List.of(3L, 4L), relecteurs);
        assertEquals(relecteurs.size(), relecteurs.stream().distinct().count(), "deux fois le même relecteur");
        assertEquals(3L, assignation.orElseThrow().getRelecteurId(),
                "la relecture rendue au dépôt est la première assignée");
    }

    // §7 : un seul étudiant éligible comme pair. Une seule relecture est créée — l'exercice est relu
    // une fois plutôt que pas du tout —, et sa note restera provisoire (cf. NoteRetenueTest).
    @Test
    void un_seul_pair_disponible_donne_une_seule_relecture() {
        presencesDeLaSession(1L, 2L);
        when(tirage.tirer(anyList())).thenReturn(2L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        verify(relectures, times(1)).enregistrer(any(Relecture.class));
        assertTrue(assignation.isPresent());
        assertEquals(2L, assignation.get().getRelecteurId());
    }

    @Test
    void enregistre_la_relecture_assignee_au_relecteur_tire() {
        presencesDeLaSession(1L, 2L, 3L);
        when(tirage.tirer(anyList())).thenReturn(2L, 3L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        assertTrue(assignation.isPresent());
        assertEquals(10L, assignation.get().getExerciceId());
        assertEquals(StatutRelecture.ASSIGNEE, assignation.get().getStatut());
    }

    @Test
    void ne_cree_aucune_relecture_si_personne_dautre_nest_present() {
        // Seul l'auteur est présent : aucun candidat éligible. L'exercice reste sans relecture plutôt
        // que d'être attribué à son propre auteur (§7).
        presencesDeLaSession(1L);

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        assertFalse(assignation.isPresent());
        verify(relectures, never()).enregistrer(any(Relecture.class));
    }

    // D4, révisé à l'étape 3 : l'exercice reste EN_ATTENTE_RELECTURE tant qu'une note manque, même si
    // la sienne vient d'être rendue. C'est ce qui distingue « corrigé » de « en cours de correction ».
    @Test
    void un_exercice_reste_en_attente_tant_quil_manque_une_note() {
        Relecture aRendre = relectureAssignee(5L, 10L, 2L);
        Exercice exercice = exerciceEnAttente();
        when(relectures.trouverParId(5L)).thenReturn(Optional.of(aRendre));
        when(exercices.trouverParId(10L)).thenReturn(Optional.of(exercice));
        when(sessions.trouverParId(1L)).thenReturn(Optional.of(sessionOuverte()));
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));
        // Le second pair n'a pas rendu. La liste est lue après le rendu : `aRendre` porte donc déjà
        // son statut RENDUE, et la seconde relecture reste ASSIGNEE.
        when(relectures.trouverParExerciceId(10L))
                .thenReturn(List.of(aRendre, relectureAssignee(6L, 10L, 3L)));

        service.rendre(5L, new RendreRelectureRequest(12, "premier rendu"));

        assertEquals(StatutExercice.EN_ATTENTE_RELECTURE, exercice.getStatut());
        verify(exercices, never()).enregistrer(any(Exercice.class));
    }

    @Test
    void un_exercice_passe_relu_a_la_derniere_note() {
        Relecture aRendre = relectureAssignee(5L, 10L, 2L);
        Relecture dejaRendue = relectureAssignee(6L, 10L, 3L);
        dejaRendue.rendre(15, "déjà rendue");
        Exercice exercice = exerciceEnAttente();
        when(relectures.trouverParId(5L)).thenReturn(Optional.of(aRendre));
        when(exercices.trouverParId(10L)).thenReturn(Optional.of(exercice));
        when(sessions.trouverParId(1L)).thenReturn(Optional.of(sessionOuverte()));
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));
        when(relectures.trouverParExerciceId(10L)).thenReturn(List.of(aRendre, dejaRendue));

        service.rendre(5L, new RendreRelectureRequest(12, "second rendu"));

        assertEquals(StatutExercice.RELU, exercice.getStatut());
    }

    private void presencesDeLaSession(Long... etudiantIds) {
        List<Presence> releve = java.util.Arrays.stream(etudiantIds).map(id -> {
            Presence presence = new Presence();
            presence.setEtudiantId(id);
            presence.setSessionId(1L);
            presence.setHorodatage(Instant.now());
            presence.setSource(SourcePresence.ETUDIANT);
            return presence;
        }).toList();
        when(presences.trouverParSession(1L)).thenReturn(releve);
    }

    private Relecture relectureAssignee(Long id, Long exerciceId, Long relecteurId) {
        Relecture relecture = new Relecture();
        relecture.setId(id);
        relecture.setExerciceId(exerciceId);
        relecture.setRelecteurId(relecteurId);
        relecture.setStatut(StatutRelecture.ASSIGNEE);
        relecture.setAssigneeAt(Instant.now());
        return relecture;
    }

    private Exercice exerciceEnAttente() {
        Exercice exercice = new Exercice();
        exercice.setId(10L);
        exercice.setSessionId(1L);
        exercice.setEtudiantId(99L);
        exercice.setLien("https://exemple.test/travail.pdf");
        exercice.setStatut(StatutExercice.EN_ATTENTE_RELECTURE);
        return exercice;
    }

    private Session sessionOuverte() {
        Session session = new Session();
        session.setId(1L);
        session.setStatut(StatutSession.OUVERTE);
        return session;
    }
}
