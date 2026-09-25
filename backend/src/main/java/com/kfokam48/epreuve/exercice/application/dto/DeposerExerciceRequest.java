package com.kfokam48.epreuve.exercice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/exercices} — champs requis : {@code sessionId}, {@code etudiantId},
 * {@code lien} (uri). La validation de forme est faite ici (B4), la validité du lien dans le domaine
 * ({@code Exercice.verifierLien}).
 */
public record DeposerExerciceRequest(
        @NotNull Long sessionId,
        @NotNull Long etudiantId,
        @NotBlank String lien
) {
}
