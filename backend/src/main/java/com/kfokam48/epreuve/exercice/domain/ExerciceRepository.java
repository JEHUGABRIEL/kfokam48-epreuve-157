package com.kfokam48.epreuve.exercice.domain;

import com.kfokam48.epreuve.exercice.domain.model.Exercice;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Port de persistance du domaine : pas de Spring Data ici, l'implémentation vit en
 * {@code infrastructure/persistence/}.
 */
public interface ExerciceRepository {

    Exercice enregistrer(Exercice exercice);

    Optional<Exercice> trouverParId(Long id);

    /** RG9 : un étudiant dépose au plus un exercice par session (unicité également portée par la base). */
    Optional<Exercice> trouverParSessionEtEtudiant(Long sessionId, Long etudiantId);

    /**
     * EF9 / RG14 / ENF2 : le tableau a besoin du nombre d'exercices déposés par étudiant. Le comptage
     * est groupé en une requête, pas une par étudiant — c'est ce qui tient la promesse de temps de
     * réponse pour une promotion de 60 étudiants.
     */
    Map<Long, Long> compterParEtudiant(Collection<Long> etudiantIds);
}
