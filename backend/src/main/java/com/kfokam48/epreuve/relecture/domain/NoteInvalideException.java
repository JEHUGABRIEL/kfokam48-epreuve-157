package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.common.error.ApiException;
import org.springframework.http.HttpStatus;

/**
 * NOTE_INVALIDE (400) — RG3 : une note est un entier compris entre 0 et 20.
 */
public class NoteInvalideException extends ApiException {

    public NoteInvalideException() {
        super("NOTE_INVALIDE", HttpStatus.BAD_REQUEST, "La note doit être un entier compris entre 0 et 20.");
    }
}
