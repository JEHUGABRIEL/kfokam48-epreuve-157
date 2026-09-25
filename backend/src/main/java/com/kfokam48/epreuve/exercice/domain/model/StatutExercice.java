package com.kfokam48.epreuve.exercice.domain.model;

/**
 * Cycle de vie d'un exercice (cf. D2 et D4).
 *
 * <ul>
 *   <li>{@code DEPOSE} — le lien est enregistré, aucun relecteur n'a pu être tiré au sort (limite
 *       connue, cf. cahier des charges §7).</li>
 *   <li>{@code EN_ATTENTE_RELECTURE} — un relecteur est assigné (EF5), il n'a pas encore rendu.</li>
 *   <li>{@code RELU} — la relecture est rendue (EF6).</li>
 * </ul>
 */
public enum StatutExercice {
    DEPOSE,
    EN_ATTENTE_RELECTURE,
    RELU
}
