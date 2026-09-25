package com.kfokam48.epreuve.common.referentiel.infrastructure;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.pagination.infrastructure.ReponsePaginee;
import com.kfokam48.epreuve.common.referentiel.application.ReferentielService;
import com.kfokam48.epreuve.common.referentiel.application.dto.EtudiantResponse;
import com.kfokam48.epreuve.common.referentiel.application.dto.PromotionResponse;

import org.springframework.http.ResponseEntity;
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
 * Les deux acceptent {@code page} et {@code taille}, optionnels. Sans eux, la collection entière est
 * rendue : c'est le comportement d'origine, et il ne doit pas changer. Le corps reste un tableau JSON ;
 * le total avant découpage est publié dans l'en-tête {@code X-Total-Count}.
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
    public ResponseEntity<List<PromotionResponse>> promotions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille) {
        return ReponsePaginee.de(referentielService.listerPromotions(PageDemandee.depuis(page, taille)));
    }

    @GetMapping("/etudiants")
    public ResponseEntity<List<EtudiantResponse>> etudiants(
            @RequestParam Long promotionId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille) {
        return ReponsePaginee.de(
                referentielService.listerEtudiants(promotionId, PageDemandee.depuis(page, taille)));
    }
}
