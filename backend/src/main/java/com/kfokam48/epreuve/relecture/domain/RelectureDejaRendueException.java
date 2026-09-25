package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * RELECTURE_DEJA_RENDUE (409) — RG12 : une relecture rendue est définitive.
 */
public class RelectureDejaRendueException extends ApiException {

    public RelectureDejaRendueException() {
        super("RELECTURE_DEJA_RENDUE", HttpStatus.CONFLICT,
                "Cette relecture a déjà été rendue et ne peut plus être modifiée.");
    }
}
