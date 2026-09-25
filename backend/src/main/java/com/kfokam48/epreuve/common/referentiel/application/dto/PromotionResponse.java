package com.kfokam48.epreuve.common.referentiel.application.dto;

/** Élément de {@code GET /api/promotions} — champs requis : {@code id}, {@code nom}. */
public record PromotionResponse(Long id, String nom) {
}
