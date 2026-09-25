package com.kfokam48.epreuve.relecture.domain.model;

/**
 * Cycle de vie d'une relecture (cf. D2).
 *
 * <p>Deux états seulement, et c'est un choix : Q13 parle d'une relecture « commencée », aucun état ne
 * la représente donc la fenêtre de remplacement du lien (RG10) court jusqu'au rendu. L'écart avec la
 * lettre de Q13 est écrit en §7 du cahier des charges plutôt que laissé implicite.
 */
public enum StatutRelecture {
    ASSIGNEE,
    RENDUE
}
