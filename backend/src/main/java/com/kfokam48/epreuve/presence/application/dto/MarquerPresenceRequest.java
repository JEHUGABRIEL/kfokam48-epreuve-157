package com.kfokam48.epreuve.presence.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corps de {@code POST /api/presences} — champs requis : {@code code}, {@code etudiantId}. */
public record MarquerPresenceRequest(
        @NotBlank String code,
        @NotNull Long etudiantId
) {
}
