package com.kfokam48.epreuve.common.referentiel.infrastructure;

import com.kfokam48.epreuve.common.referentiel.application.ReferentielService;
import com.kfokam48.epreuve.common.referentiel.application.dto.EtudiantResponse;
import com.kfokam48.epreuve.common.referentiel.application.dto.PromotionResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapte le référentiel au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code GET /api/promotions} → 200</li>
 *   <li>{@code GET /api/etudiants?promotionId=} → 200, 404 (PROMOTION_INCONNUE)</li>
 * </ul>
 * Aucune logique ici : le contrôleur traduit le HTTP, le service décide, le contrôleur répond.
 */
@RestController
@RequestMapping("/api")
public class ReferentielController {

    private final ReferentielService referentielService;

    public ReferentielController(ReferentielService referentielService) {
        this.referentielService = referentielService;
    }

    @GetMapping("/promotions")
    public List<PromotionResponse> promotions() {
        return referentielService.listerPromotions();
    }

    @GetMapping("/etudiants")
    public List<EtudiantResponse> etudiants(@RequestParam Long promotionId) {
        return referentielService.listerEtudiants(promotionId);
    }
}
