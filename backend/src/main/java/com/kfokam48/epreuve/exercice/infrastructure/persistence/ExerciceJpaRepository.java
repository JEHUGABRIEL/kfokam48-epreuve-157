package com.kfokam48.epreuve.exercice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Dépôt Spring Data : détail de stockage, injecté par l'adaptateur seulement. */
public interface ExerciceJpaRepository extends JpaRepository<ExerciceEntity, Long> {

    Optional<ExerciceEntity> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /**
     * Comptage groupé par étudiant (EF9, ENF2). Une seule requête pour toute la promotion : la ligne du
     * tableau ne déclenche aucun comptage individuel.
     */
    @Query("""
            select e.etudiantId, count(e)
            from ExerciceEntity e
            where e.etudiantId in :etudiantIds
            group by e.etudiantId
            """)
    List<Object[]> compterParEtudiant(@Param("etudiantIds") Collection<Long> etudiantIds);
}
