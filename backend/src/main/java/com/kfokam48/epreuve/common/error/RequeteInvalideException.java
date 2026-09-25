package com.kfokam48.epreuve.common.error;

import org.springframework.http.HttpStatus;

/**
 * Erreur de forme : une entrée que le contrat ne nomme pas. Le contrat impose le format
 * {@code { code, message }} mais ne fixe aucun code pour une valeur hors bornes, par exemple — le
 * cahier des charges §7 a tranché en regroupant ces cas sous un code unique, {@code REQUETE_INVALIDE}.
 *
 * <p>Elle existe pour que les couches qui ne sont pas des contrôleurs — ici la pagination — puissent
 * refuser une entrée invalide dans le même format que Spring MVC, sans dupliquer le code ailleurs.
 */
public class RequeteInvalideException extends ApiException {

    /** Code unique des erreurs de forme (cf. cahier des charges §7). */
    public static final String CODE = "REQUETE_INVALIDE";

    public RequeteInvalideException(String message) {
        super(CODE, HttpStatus.BAD_REQUEST, message);
    }
}
