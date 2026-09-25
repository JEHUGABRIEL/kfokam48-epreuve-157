package com.kfokam48.epreuve.common.error;

import org.springframework.http.HttpStatus;

/**
 * Racine des erreurs métier. Chaque sous-classe porte le {@code code} stable imposé par le
 * contrat et le statut HTTP à renvoyer. Le message est rendu au client tel quel : jamais de
 * stack trace (B4).
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus statut;

    protected ApiException(String code, HttpStatus statut, String message) {
        super(message);
        this.code = code;
        this.statut = statut;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}
