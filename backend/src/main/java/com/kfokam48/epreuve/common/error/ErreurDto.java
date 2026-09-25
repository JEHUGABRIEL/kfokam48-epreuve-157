package com.kfokam48.epreuve.common.error;

/**
 * Format d'erreur imposé par le contrat (B4) : {@code { code, message }}, sans exception.
 * {@code code} est un identifiant stable en majuscules, {@code message} une phrase en français.
 */
public record ErreurDto(String code, String message) {
}
