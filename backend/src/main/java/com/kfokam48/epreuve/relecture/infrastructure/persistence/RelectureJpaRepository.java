package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Dépôt Spring Data : détail de stockage, injecté par l'adaptateur seulement. */
public interface RelectureJpaRepository extends JpaRepository<RelectureEntity, Long> {

    Optional<RelectureEntity> findByExerciceId(Long exerciceId);

    List<RelectureEntity> findByRelecteurIdAndStatut(Long relecteurId, StatutRelecture statut);

    /** Même lecture, découpée (pagination optionnelle). */
    Page<RelectureEntity> findByRelecteurIdAndStatut(Long relecteurId, StatutRelecture statut,
                                                     Pageable pageable);

    long countByRelecteurIdAndStatut(Long relecteurId, StatutRelecture statut);

    /**
     * Moyenne des notes reçues par chaque auteur d'exercice (EF9, RG14, ENF2).
     *
     * <p>La jointure relie la relecture à l'exercice, puis l'exercice à son auteur : la note est reçue
     * par celui qui a déposé, pas par celui qui a relu. Seules les relectures rendues comptent
     * ({@code note is not null}), pour qu'un étudiant sans note n'apparaisse pas avec une moyenne nulle.
     */
    @Query("""
            select e.etudiantId, avg(r.note)
            from RelectureEntity r, ExerciceEntity e
            where r.exerciceId = e.id
              and r.note is not null
              and e.etudiantId in :etudiantIds
            group by e.etudiantId
            """)
    List<Object[]> moyenneParAuteur(@Param("etudiantIds") Collection<Long> etudiantIds);

    /** Relectures encore à rendre, par relecteur (EF9, RG14 — Q11 : « je dois le voir clairement »). */
    @Query("""
            select r.relecteurId, count(r)
            from RelectureEntity r
            where r.relecteurId in :etudiantIds
              and r.statut = :statut
            group by r.relecteurId
            """)
    List<Object[]> compterParRelecteurEtStatut(@Param("etudiantIds") Collection<Long> etudiantIds,
                                               @Param("statut") StatutRelecture statut);
}
