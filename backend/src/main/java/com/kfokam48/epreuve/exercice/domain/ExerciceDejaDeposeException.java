package com.kfokam48.epreuve.exercice.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * EXERCICE_DEJA_DEPOSE (409) — {@code POST /api/exercices}.
 * Voir aussi RG10 : le remplacement d'un lien passe par une autre opération.
 */
public class ExerciceDejaDeposeException extends ApiException {

    public ExerciceDejaDeposeException() {
        super("EXERCICE_DEJA_DEPOSE", HttpStatus.CONFLICT, "Un exercice a déjà été déposé pour cette session.");
    }
}
