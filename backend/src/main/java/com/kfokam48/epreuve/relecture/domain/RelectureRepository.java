package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import java.util.List;
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

    /**
     * Ce qu'un relecteur doit encore rendre (UC7). C'est cette lecture qui rend l'opération imposée
     * {@code POST /api/relectures/{id}} atteignable : sans elle, le relecteur ignore l'identifiant à
     * passer en chemin.
     */
    List<Relecture> listerParRelecteurEtStatut(Long relecteurId, StatutRelecture statut);
}
