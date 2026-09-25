package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * CODE_INCONNU (400) — {@code POST /api/presences}.
 */
public class CodeInconnuException extends ApiException {

    public CodeInconnuException() {
        super("CODE_INCONNU", HttpStatus.BAD_REQUEST, "Code de présence inconnu.");
    }
}
