package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dépôt Spring Data : détail de stockage, injecté par l'adaptateur uniquement.
 */
public interface EtudiantJpaRepository extends JpaRepository<EtudiantEntity, Long> {

    /** Trié par nom : le tableau du formateur (EF9) doit se lire dans un ordre stable. */
    List<EtudiantEntity> findByPromotionIdOrderByNomAsc(Long promotionId);

    /** Le même tri, découpé — la pagination est optionnelle, elle ne remplace jamais l'autre lecture. */
    Page<EtudiantEntity> findByPromotionId(Long promotionId, Pageable pageable);

    long countByPromotionId(Long promotionId);
}
