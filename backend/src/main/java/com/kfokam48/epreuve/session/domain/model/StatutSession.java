package com.kfokam48.epreuve.session.domain.model;

/**
 * Cycle de vie d'une session (cf. D2). Concept du domaine, partagé avec la couche de stockage :
 * c'est {@code infrastructure/persistence/} qui décide comment il est écrit en base (ici, en texte).
 */
public enum StatutSession {
    OUVERTE,
    CLOTUREE
}
