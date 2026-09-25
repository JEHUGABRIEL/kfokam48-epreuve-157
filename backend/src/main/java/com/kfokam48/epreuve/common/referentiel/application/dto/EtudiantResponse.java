package com.kfokam48.epreuve.common.referentiel.application.dto;

/**
 * Élément de {@code GET /api/etudiants?promotionId=} — champs requis : {@code id}, {@code nom}.
 * Sert à l'identification par choix dans une liste (Q1).
 */
public record EtudiantResponse(Long id, String nom) {
}
