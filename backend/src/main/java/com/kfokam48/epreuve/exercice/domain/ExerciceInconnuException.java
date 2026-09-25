package com.kfokam48.epreuve.exercice.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * EXERCICE_INCONNU (404) — l'identifiant désigne un exercice qui n'existe pas
 * ({@code PUT /api/exercices/{id}}, et contrôle défensif au rendu d'une relecture).
 *
 * <p>Même famille que {@code SESSION_INCONNUE} et {@code PROMOTION_INCONNUE} : un identifiant en
 * entrée qui ne désigne rien donne 404, pas 400.
 */
public class ExerciceInconnuException extends ApiException {

    public ExerciceInconnuException(Long id) {
        super("EXERCICE_INCONNU", HttpStatus.NOT_FOUND, "Aucun exercice trouvé pour l'identifiant " + id + ".");
    }
}
