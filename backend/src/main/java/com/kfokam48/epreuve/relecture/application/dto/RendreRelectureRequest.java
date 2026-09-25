package com.kfokam48.epreuve.relecture.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/relectures/{id}} — champs requis : {@code note}, {@code commentaire}.
 *
 * <p>Volontairement sans {@code @Min}/{@code @Max} sur la note : le contrat impose le code
 * {@code NOTE_INVALIDE} pour une note hors 0–20, or une contrainte de validation produirait
 * {@code REQUETE_INVALIDE} avant même d'entrer dans le service. La borne est donc vérifiée par le
 * domaine (RG3), seul endroit qui parle le langage du client.
 */
public record RendreRelectureRequest(
        @NotNull Integer note,
        @NotNull String commentaire
) {
}
