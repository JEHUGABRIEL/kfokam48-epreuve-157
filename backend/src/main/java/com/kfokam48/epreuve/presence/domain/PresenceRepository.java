package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.presence.domain.model.Presence;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance du domaine : pas de dépendance à Spring Data ici, l'implémentation vit dans
 * {@code infrastructure/persistence/}.
 */
public interface PresenceRepository {

    Presence enregistrer(Presence presence);

    /** RG7 : sert à refuser une seconde présence du même étudiant à la même session. */
    Optional<Presence> trouverParSessionEtEtudiant(Long sessionId, Long etudiantId);

    /** RG5 : le tirage au sort du relecteur se fait parmi les présents de la session. */
    List<Presence> trouverParSession(Long sessionId);
}
