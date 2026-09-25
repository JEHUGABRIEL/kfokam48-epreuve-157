package com.kfokam48.epreuve.common.referentiel.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * PROMOTION_INCONNUE (404) — {@code GET /api/tableau?promotionId=}.
 */
public class PromotionInconnueException extends ApiException {

    public PromotionInconnueException() {
        super("PROMOTION_INCONNUE", HttpStatus.NOT_FOUND, "Promotion inconnue.");
    }
}
