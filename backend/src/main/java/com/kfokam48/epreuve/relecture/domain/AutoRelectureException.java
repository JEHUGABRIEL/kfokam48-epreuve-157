package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * AUTO_RELECTURE (403) — RG2 : un étudiant ne peut pas relire son propre exercice.
 */
public class AutoRelectureException extends ApiException {

    public AutoRelectureException() {
        super("AUTO_RELECTURE", HttpStatus.FORBIDDEN, "Un étudiant ne peut pas relire son propre exercice.");
    }
}
