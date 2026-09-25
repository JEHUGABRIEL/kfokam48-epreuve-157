package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * DEJA_PRESENT (409) — RG7 : une seule présence par étudiant et par session.
 */
public class DejaPresentException extends ApiException {

    public DejaPresentException() {
        super("DEJA_PRESENT", HttpStatus.CONFLICT, "Présence déjà enregistrée pour cette session.");
    }
}
