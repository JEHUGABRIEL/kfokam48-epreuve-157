package com.kfokam48.epreuve.session.infrastructure.persistence;

import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;
import com.kfokam48.epreuve.session.domain.model.StatutSession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L'adaptateur est testé **par le port**, jamais par son implémentation : c'est ce que la couche
 * application voit, et c'est ce qui prouve que le domaine peut ignorer l'entité de stockage.
 *
 * <p>Le profil {@code test} utilise H2 en mémoire (Flyway désactivé) : ce test ne demande donc ni
 * Docker ni PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class SessionRepositoryAdapterTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void enregistre_une_session_en_lui_attribuant_un_identifiant() {
        Session enregistree = sessionRepository.enregistrer(nouvelleSession("CODE01"));

        assertThat(enregistree.getId()).isNotNull();
        assertThat(sessionRepository.trouverParId(enregistree.getId()))
                .isPresent()
                .get()
                .satisfies(session -> assertThat(session.getCode()).isEqualTo("CODE01"));
    }

    @Test
    void retrouve_une_session_par_son_code() {
        sessionRepository.enregistrer(nouvelleSession("CODE02"));

        assertThat(sessionRepository.trouverParCode("CODE02"))
                .isPresent()
                .get()
                .satisfies(session -> {
                    assertThat(session.getTitre()).isEqualTo("Session démo");
                    assertThat(session.getStatut()).isEqualTo(StatutSession.OUVERTE);
                });
    }

    @Test
    void ne_trouve_rien_pour_un_code_inconnu() {
        assertThat(sessionRepository.trouverParCode("INEXISTANT")).isEmpty();
    }

    private Session nouvelleSession(String code) {
        Session session = new Session();
        session.setTitre("Session démo");
        session.setPromotionId(1L);
        session.setCode(code);
        session.setOuvertureAt(Instant.parse("2026-09-25T09:00:00Z"));
        session.setExpirationAt(Instant.parse("2026-09-25T09:15:00Z"));
        return session;
    }
}
