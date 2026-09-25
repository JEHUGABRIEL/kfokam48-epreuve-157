package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * TROP_DE_TENTATIVES (429) — RG8 : après 5 échecs de code, le même étudiant est bloqué 2 minutes.
 *
 * <p>Statut ajouté hors contrat imposé : l'Annexe B n'en prévoyait aucun pour une tentative bloquée,
 * et sans lui RG8 n'était observable ni par le client ni par un test (cf. cahier des charges §7).
 */
public class TropDeTentativesException extends ApiException {

    public TropDeTentativesException() {
        super("TROP_DE_TENTATIVES", HttpStatus.TOO_MANY_REQUESTS,
                "Trop de tentatives. Réessayez dans deux minutes.");
    }
}
