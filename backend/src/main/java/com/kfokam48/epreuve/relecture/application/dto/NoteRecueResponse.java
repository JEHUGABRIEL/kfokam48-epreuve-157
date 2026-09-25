package com.kfokam48.epreuve.relecture.application.dto;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

/**
 * La note que l'étudiant relu reçoit, et son caractère provisoire (EF7, RG6, RG4 révisée).
 *
 * <p>Volontairement distinct de {@link RelectureResponse}, et c'est tout l'intérêt de ce DTO :
 *
 * <ul>
 *   <li>la réponse de l'opération <strong>imposée</strong> {@code POST /api/relectures/{id}} porte la
 *       note d'<em>un</em> relecteur — un entier, exactement comme le contrat imposé le décrit ;</li>
 *   <li>celle-ci porte la note <em>retenue</em> de l'exercice, qui peut être une moyenne non entière et
 *       qui peut n'être que provisoire.</li>
 * </ul>
 *
 * <p>Les confondre aurait fait entrer une décimale et un booléen dans la réponse d'une opération
 * imposée — c'est-à-dire modifier un contrat gelé pour les besoins d'un changement. Les deux formes
 * existent parce que les deux questions n'ont rien à voir : « quelle note ai-je mise ? » et « quelle
 * note me revient ? ».
 *
 * <p>Ne transporte ni {@code relecteurId} ni aucun nom : RG6 (Q8) n'a pas bougé avec le changement de
 * besoin, il est même plus exigeant — deux relecteurs, deux fois plus d'identités à ne pas laisser
 * fuir.
 */
public record NoteRecueResponse(StatutRelecture statut, Double note, String commentaire, boolean provisoire) {
}
