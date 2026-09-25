package com.kfokam48.epreuve.common.pagination.domain;

import com.kfokam48.epreuve.common.error.RequeteInvalideException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire (B6) de la pagination : pas de contexte Spring, pas de base.
 *
 * <p>Le premier test est le plus important du fichier : il protège le contrat imposé. Si
 * {@code depuis(null, null)} se mettait un jour à rendre une page par défaut de 20 lignes,
 * {@code GET /api/tableau?promotionId=} cesserait de rendre la promotion entière — une opération
 * imposée se mettrait à mentir sans qu'aucun de ses codes de statut ne change.
 */
class PageDemandeeTest {

    @Test
    void aucune_pagination_quand_les_deux_parametres_sont_absents() {
        assertTrue(PageDemandee.depuis(null, null).isEmpty(),
                "sans paramètre, la lecture doit rester celle du contrat imposé : tout");
    }

    @Test
    void ne_complete_que_le_parametre_absent() {
        // Le défaut ne s'applique qu'à ce qui manque : une taille demandée n'est jamais écrasée.
        assertEquals(new PageDemandee(1, 5), PageDemandee.depuis(null, 5).orElseThrow());
        assertEquals(new PageDemandee(3, PageDemandee.TAILLE_PAR_DEFAUT),
                PageDemandee.depuis(3, null).orElseThrow());
    }

    @Test
    void refuse_une_page_hors_bornes() {
        RequeteInvalideException erreur =
                assertThrows(RequeteInvalideException.class, () -> PageDemandee.depuis(0, 20));

        assertEquals("REQUETE_INVALIDE", erreur.getCode(), "le contrat ne nomme pas de code pour ces cas");
        assertEquals(HttpStatus.BAD_REQUEST, erreur.getStatut());
    }

    @Test
    void refuse_une_taille_hors_bornes() {
        assertThrows(RequeteInvalideException.class, () -> PageDemandee.depuis(1, 0));
        assertThrows(RequeteInvalideException.class,
                () -> PageDemandee.depuis(1, PageDemandee.TAILLE_MAXIMALE + 1));
    }

    @Test
    void calcule_l_index_du_premier_element_de_la_page() {
        assertEquals(0, new PageDemandee(1, 20).premierIndex());
        assertEquals(20, new PageDemandee(2, 20).premierIndex());
        assertEquals(40, new PageDemandee(3, 20).premierIndex());
    }
}
