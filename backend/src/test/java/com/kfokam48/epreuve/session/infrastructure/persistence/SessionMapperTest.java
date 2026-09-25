package com.kfokam48.epreuve.session.infrastructure.persistence;

import com.kfokam48.epreuve.session.domain.model.Session;
import com.kfokam48.epreuve.session.domain.model.StatutSession;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le mapper est le seul point de contact entre le modèle et la table : un champ oublié ici se
 * traduirait par une donnée perdue en base, sans que rien ne le signale. L'aller-retour est donc
 * vérifié champ par champ, sur un objet portant une valeur distincte dans chacun d'eux.
 */
class SessionMapperTest {

    private final SessionMapper mapper = new SessionMapper();

    @Test
    void fait_un_aller_et_retour_sans_perdre_de_champ() {
        Session modele = new Session();
        modele.setId(7L);
        modele.setTitre("Session démo");
        modele.setPromotionId(1L);
        modele.setCode("ABC234");
        modele.setOuvertureAt(Instant.parse("2026-09-25T10:00:00Z"));
        modele.setExpirationAt(Instant.parse("2026-09-25T10:15:00Z"));
        modele.setClotureAt(Instant.parse("2026-09-25T10:30:00Z"));
        modele.setStatut(StatutSession.CLOTUREE);

        Session retour = mapper.versModele(mapper.versEntite(modele));

        assertThat(retour.getId()).isEqualTo(7L);
        assertThat(retour.getTitre()).isEqualTo("Session démo");
        assertThat(retour.getPromotionId()).isEqualTo(1L);
        assertThat(retour.getCode()).isEqualTo("ABC234");
        assertThat(retour.getOuvertureAt()).isEqualTo(Instant.parse("2026-09-25T10:00:00Z"));
        assertThat(retour.getExpirationAt()).isEqualTo(Instant.parse("2026-09-25T10:15:00Z"));
        assertThat(retour.getClotureAt()).isEqualTo(Instant.parse("2026-09-25T10:30:00Z"));
        assertThat(retour.getStatut()).isEqualTo(StatutSession.CLOTUREE);
    }
}
