package com.kfokam48.epreuve.session.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class SessionDejaClotureeException extends ApiException {
    public SessionDejaClotureeException(Long id) {
        super("SESSION_DEJA_CLOTUREE", HttpStatus.CONFLICT,
                "La session " + id + " est déjà clôturée.");
    }
}
