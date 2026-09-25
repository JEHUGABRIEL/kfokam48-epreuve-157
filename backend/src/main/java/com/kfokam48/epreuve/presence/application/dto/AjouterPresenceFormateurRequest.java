package com.kfokam48.epreuve.presence.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/presences/formateur} — champs requis : {@code sessionId},
 * {@code etudiantId}.
 *
 * <p>Pas de code de présence dans le corps, et c'est le fond de Q14 : le formateur ajoute la présence
 * précisément parce que l'étudiant n'a pas pu saisir le code lui-même.
 */
public record AjouterPresenceFormateurRequest(
        @NotNull Long sessionId,
        @NotNull Long etudiantId
) {
}
