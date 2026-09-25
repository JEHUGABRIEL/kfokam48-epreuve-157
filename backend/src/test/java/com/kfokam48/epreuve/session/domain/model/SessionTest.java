package com.kfokam48.epreuve.session.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Règles métier du domaine, testées seules : ni base de données, ni contexte Spring. C'est
 * exactement ce que permet la séparation entre le modèle et l'entité de stockage.
 */
class SessionTest {

    // Vérifie RG1 : le code expire 15 minutes après l'ouverture, pas avant, pas après.
    @Test
    void une_session_est_expiree_apres_15_minutes() {
        Session session = new Session();
        Instant ouverture = Instant.now().minusSeconds(16 * 60);
        session.setOuvertureAt(ouverture);
        session.setExpirationAt(ouverture.plusSeconds(15 * 60));

        assertThat(session.estExpiree()).isTrue();
    }

    @Test
    void une_session_n_est_pas_expiree_avant_15_minutes() {
        Session session = new Session();
        Instant ouverture = Instant.now().minusSeconds(5 * 60);
        session.setOuvertureAt(ouverture);
        session.setExpirationAt(ouverture.plusSeconds(15 * 60));

        assertThat(session.estExpiree()).isFalse();
    }

    // Vérifie RG13 : la clôture est un état propre, indépendant de l'expiration du code.
    @Test
    void une_session_cloturee_est_reconnue_comme_telle() {
        Session session = new Session();
        session.setStatut(StatutSession.OUVERTE);
        assertThat(session.estCloturee()).isFalse();

        session.setStatut(StatutSession.CLOTUREE);
        assertThat(session.estCloturee()).isTrue();
    }
}
