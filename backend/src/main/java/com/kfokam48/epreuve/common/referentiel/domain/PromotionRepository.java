package com.kfokam48.epreuve.common.referentiel.domain;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.referentiel.domain.model.Promotion;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance du domaine : ce dont le domaine a besoin, dit dans son langage.
 *
 * <p>Cette interface n'étend pas {@code JpaRepository} : l'implémentation vit en
 * {@code infrastructure/persistence/}. Les promotions ne sont jamais créées par l'application — elles
 * viennent des migrations — donc le port n'expose que la lecture.
 */
public interface PromotionRepository {

    Optional<Promotion> trouverParId(Long id);

    List<Promotion> listerToutes();

    /** La même lecture, découpée. */
    List<Promotion> listerToutes(PageDemandee page);

    /** Nombre de promotions avant découpage — alimente l'en-tête X-Total-Count. */
    long compterToutes();
}
