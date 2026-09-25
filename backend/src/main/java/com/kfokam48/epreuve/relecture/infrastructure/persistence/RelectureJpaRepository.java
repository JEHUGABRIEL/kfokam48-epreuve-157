package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Dépôt Spring Data : détail de stockage, injecté par l'adaptateur seulement. */
public interface RelectureJpaRepository extends JpaRepository<RelectureEntity, Long> {

    Optional<RelectureEntity> findByExerciceId(Long exerciceId);
}
