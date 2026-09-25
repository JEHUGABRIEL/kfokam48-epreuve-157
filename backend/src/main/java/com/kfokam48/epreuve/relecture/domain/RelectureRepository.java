package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.relecture.domain.model.Relecture;

import java.util.Optional;

/**
 * Port de persistance du domaine. Ce module est le seul à écrire une {@code Relecture} : l'assignation
 * au dépôt (RG5) comme le rendu de la note (RG12) passent par ici.
 */
public interface RelectureRepository {

    Relecture enregistrer(Relecture relecture);

    Optional<Relecture> trouverParId(Long id);

    /** RG4 : un exercice n'a qu'un seul relecteur — sert à vérifier l'invariant avant tout rendu. */
    Optional<Relecture> trouverParExerciceId(Long exerciceId);
}
