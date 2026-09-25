package com.kfokam48.epreuve.exercice.application.dto;

import com.kfokam48.epreuve.exercice.domain.model.StatutExercice;

/** Réponse 201 de {@code POST /api/exercices} — champs requis : {@code id}, {@code statut}. */
public record ExerciceDeposeResponse(Long id, StatutExercice statut) {
}
