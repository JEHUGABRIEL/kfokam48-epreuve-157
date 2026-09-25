package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * CODE_EXPIRE (410) — RG1 : le code expire 15 minutes après l'ouverture de la session.
 */
public class CodeExpireException extends ApiException {

    public CodeExpireException() {
        super("CODE_EXPIRE", HttpStatus.GONE, "Le code de présence a expiré.");
    }
}
