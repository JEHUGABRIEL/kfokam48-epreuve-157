package com.kfokam48.epreuve.session.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class SessionInconnueException extends ApiException {
    public SessionInconnueException(Long id) {
        super("SESSION_INCONNUE", HttpStatus.NOT_FOUND,
                "Aucune session trouvée pour l'identifiant " + id + ".");
    }
}
