package com.kfokam48.epreuve.presence.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Dépôt Spring Data : détail de stockage, injecté par l'adaptateur seulement. */
public interface PresenceJpaRepository extends JpaRepository<PresenceEntity, Long> {

    Optional<PresenceEntity> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<PresenceEntity> findBySessionId(Long sessionId);
}
