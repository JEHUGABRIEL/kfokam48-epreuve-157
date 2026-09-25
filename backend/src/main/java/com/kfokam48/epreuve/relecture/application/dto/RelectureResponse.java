package com.kfokam48.epreuve.relecture.application.dto;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

/**
 * Relecture vue de l'extérieur : {@code statut}, {@code note} et {@code commentaire} uniquement.
 *
 * <p>Ne transporte jamais {@code relecteurId} ni le nom du relecteur : RG6 (Q8) l'interdit, et ce DTO
 * est la seule forme sous laquelle une relecture sort de l'API. C'est la raison d'être du DTO ici —
 * une entité exposée aurait laissé fuir l'identité par accident.
 */
public record RelectureResponse(StatutRelecture statut, Integer note, String commentaire) {
}
