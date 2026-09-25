package com.kfokam48.epreuve.presence.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Dépôt Spring Data : détail de stockage, injecté par l'adaptateur seulement. */
public interface PresenceJpaRepository extends JpaRepository<PresenceEntity, Long> {

    Optional<PresenceEntity> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<PresenceEntity> findBySessionId(Long sessionId);

    /** Comptage groupé par étudiant pour le tableau du formateur (EF9, ENF2). */
    @Query("""
            select p.etudiantId, count(p)
            from PresenceEntity p
            where p.etudiantId in :etudiantIds
            group by p.etudiantId
            """)
    List<Object[]> compterParEtudiant(@Param("etudiantIds") Collection<Long> etudiantIds);
}
