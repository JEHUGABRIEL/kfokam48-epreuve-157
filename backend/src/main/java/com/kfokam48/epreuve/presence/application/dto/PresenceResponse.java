package com.kfokam48.epreuve.presence.application.dto;

import com.kfokam48.epreuve.presence.domain.model.SourcePresence;

/**
 * Réponse 201 de {@code POST /api/presences} — champs requis par le contrat : {@code id},
 * {@code sessionId}, {@code etudiantId}, {@code source}.
 */
public record PresenceResponse(
        Long id,
        Long sessionId,
        Long etudiantId,
        SourcePresence source
) {
}
