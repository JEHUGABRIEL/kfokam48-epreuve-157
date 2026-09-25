package com.kfokam48.epreuve.common.referentiel.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionInconnueException;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test unitaire (B6) : pas de contexte Spring, pas de base. La règle vérifiée est celle du contrat —
 * une promotion inconnue donne PROMOTION_INCONNUE, comme sur {@code GET /api/tableau}.
 */
class ReferentielServiceTest {

    @Test
    void refuse_de_lister_les_etudiants_d_une_promotion_inconnue() {
        PromotionRepository promotions = mock(PromotionRepository.class);
        when(promotions.trouverParId(99L)).thenReturn(Optional.empty());

        ReferentielService service = new ReferentielService(promotions, mock(EtudiantRepository.class));

        assertThrows(PromotionInconnueException.class, () -> service.listerEtudiants(99L));
    }
}
