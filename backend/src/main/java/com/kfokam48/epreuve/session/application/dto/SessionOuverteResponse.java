package com.kfokam48.epreuve.session.application.dto;

import java.time.Instant;

public record SessionOuverteResponse(
        Long id,
        String code,
        Instant ouvertureAt,
        Instant expirationAt
) {
}
