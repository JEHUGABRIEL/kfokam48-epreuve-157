package com.kfokam48.epreuve.relecture.application.dto;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

/**
 * Une relecture à rendre, vue par le relecteur : {@code id} (celui à passer au
 * {@code POST /api/relectures/{id}}), {@code exerciceId}, {@code sessionId}, {@code lien} et
 * {@code statut}.
 *
 * <p>Ne transporte ni l'identité de l'auteur ni celle du relecteur : RG6 (Q8) vaut dans les deux sens,
 * et le relecteur sait déjà qui il est.
 */
public record RelectureAssigneeResponse(Long id, Long exerciceId, Long sessionId, String lien,
                                        StatutRelecture statut) {
}
