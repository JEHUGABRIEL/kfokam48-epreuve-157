package com.kfokam48.epreuve.tableau.infrastructure;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.pagination.infrastructure.ReponsePaginee;
import com.kfokam48.epreuve.tableau.application.TableauService;
import com.kfokam48.epreuve.tableau.application.dto.LigneTableauResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapte le module tableau au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code GET /api/tableau?promotionId=} → 200 (liste de lignes), 404
 *       {@code PROMOTION_INCONNUE}</li>
 * </ul>
 * {@code page} et {@code taille} sont optionnels et ne changent rien à ce contrat : sans eux, la
 * promotion entière est rendue, exactement comme avant. Le corps reste un tableau JSON ; le total est
 * dans {@code X-Total-Count}.
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    // EF9 / RG14 — le formateur consulte le récapitulatif d'une promotion.
    @GetMapping
    public ResponseEntity<List<LigneTableauResponse>> tableau(
            @RequestParam Long promotionId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille) {
        return ReponsePaginee.de(
                tableauService.recapitulatif(promotionId, PageDemandee.depuis(page, taille)));
    }
}
