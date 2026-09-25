package com.kfokam48.epreuve.exercice.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de {@code PUT /api/exercices/{id}} — champ requis : {@code lien} (uri). */
public record RemplacerLienRequest(@NotBlank String lien) {
}
