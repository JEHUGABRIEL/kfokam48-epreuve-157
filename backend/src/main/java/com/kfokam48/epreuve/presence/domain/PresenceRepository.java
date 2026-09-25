package com.kfokam48.epreuve.presence.domain;

import com.kfokam48.epreuve.presence.domain.model.Presence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    /**
     * EF9 / RG14 / ENF2 : nombre de présences par étudiant, en une requête groupée — pas une par
     * étudiant, sinon le tableau ferait 60 allers-retours en base.
     */
    Map<Long, Long> compterParEtudiant(Collection<Long> etudiantIds);
}
