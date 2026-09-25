package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * RELECTURE_INCONNUE (404) — l'identifiant en chemin ne désigne aucune relecture
 * ({@code POST /api/relectures/{id}}, {@code GET /api/relectures/recues}).
 */
public class RelectureInconnueException extends ApiException {

    public RelectureInconnueException(Long id) {
        super("RELECTURE_INCONNUE", HttpStatus.NOT_FOUND, "Aucune relecture trouvée pour l'identifiant " + id + ".");
    }

    /** Cas de {@code GET /api/relectures/recues} : l'étudiant n'a rien déposé pour cette session. */
    public RelectureInconnueException() {
        super("RELECTURE_INCONNUE", HttpStatus.NOT_FOUND,
                "Aucun exercice déposé par cet étudiant pour cette session, donc aucune relecture à consulter.");
    }
}
