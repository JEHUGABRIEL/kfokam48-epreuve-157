package com.kfokam48.epreuve.exercice.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * LIEN_INVALIDE (400) — {@code POST /api/exercices}.
 */
public class LienInvalideException extends ApiException {

    public LienInvalideException() {
        super("LIEN_INVALIDE", HttpStatus.BAD_REQUEST, "Le lien de l'exercice est invalide.");
    }
}
