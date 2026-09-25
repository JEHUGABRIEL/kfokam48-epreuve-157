package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Dépôt Spring Data : détail de stockage, injecté par l'adaptateur uniquement — jamais par la couche
 * application, sinon le domaine dépendrait de Spring Data par la bande.
 */
public interface PromotionJpaRepository extends JpaRepository<PromotionEntity, Long> {
}
