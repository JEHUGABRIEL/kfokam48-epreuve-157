package com.kfokam48.epreuve.session.application.dto;

import java.time.Instant;

public record SessionClotureeResponse(
        Long id,
        Instant clotureAt
) {
}
