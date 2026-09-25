package com.kfokam48.epreuve.common.referentiel.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * ETUDIANT_INCONNU (404) — l'{@code etudiantId} reçu en entrée ne correspond à personne
 * ({@code POST /api/exercices}, {@code POST /api/presences/formateur}).
 *
 * <p>404 et non 400 : le contrat imposé applique déjà cette règle à l'autre donnée de référence
 * ({@code PROMOTION_INCONNUE} en 404 sur {@code GET /api/tableau}). Un identifiant qui ne désigne rien
 * n'est pas une erreur de forme, c'est une référence inexistante. Code documenté en §7 du cahier des
 * charges, comme les autres codes ajoutés à des opérations imposées.
 */
public class EtudiantInconnuException extends ApiException {

    public EtudiantInconnuException(Long etudiantId) {
        super("ETUDIANT_INCONNU", HttpStatus.NOT_FOUND, "Étudiant inconnu : " + etudiantId + ".");
    }
}
