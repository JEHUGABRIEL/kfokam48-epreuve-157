package com.kfokam48.epreuve.relecture.domain;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Port de persistance du domaine. Ce module est le seul à écrire une {@code Relecture} : l'assignation
 * au dépôt (RG5) comme le rendu de la note (RG12) passent par ici.
 */
public interface RelectureRepository {

    Relecture enregistrer(Relecture relecture);

    Optional<Relecture> trouverParId(Long id);

    /**
     * RG4 révisée (étape 3) : un exercice est relu par deux pairs distincts, donc il porte désormais
     * <strong>jusqu'à deux</strong> relectures. Cette lecture sert à l'état de l'exercice (D4), au
     * calcul de la note retenue, et à la fermeture du remplacement de lien (RG10).
     */
    List<Relecture> trouverParExerciceId(Long exerciceId);

    /**
     * Ce qu'un relecteur doit encore rendre (UC7). C'est cette lecture qui rend l'opération imposée
     * {@code POST /api/relectures/{id}} atteignable : sans elle, le relecteur ignore l'identifiant à
     * passer en chemin.
     */
    List<Relecture> listerParRelecteurEtStatut(Long relecteurId, StatutRelecture statut);

    /** La même lecture, découpée. */
    List<Relecture> listerParRelecteurEtStatut(Long relecteurId, StatutRelecture statut, PageDemandee page);

    /** Nombre de relectures attendues par ce relecteur, avant découpage. */
    long compterParRelecteurEtStatut(Long relecteurId, StatutRelecture statut);

    /**
     * EF9 / RG14 : la moyenne des notes <strong>retenues</strong>, par auteur de l'exercice relu.
     * Absent de la carte = aucune note reçue (et non une moyenne de zéro).
     *
     * <p>Depuis l'étape 3, la moyenne porte sur les notes retenues, donc sur une moyenne par exercice :
     * additionner toutes les notes donnerait deux fois plus de poids à un exercice relu par deux pairs
     * qu'à un exercice relu par un seul.
     *
     * <p>Une seule requête agrégée pour toute la promotion : c'est ce qui tient ENF2.
     */
    Map<Long, Double> moyenneParAuteur(Collection<Long> etudiantIds);

    /** EF9 / RG14 : ce que chaque étudiant doit encore relire (Q11, Q16). */
    Map<Long, Long> compterEnAttenteParRelecteur(Collection<Long> etudiantIds);
}
