package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Promotion;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptateur : implémente le port du domaine au-dessus de Spring Data et du mapper. Il ne rend que des
 * modèles, jamais des entités de stockage.
 */
@Component
public class PromotionRepositoryAdapter implements PromotionRepository {

    private final PromotionJpaRepository jpaRepository;
    private final PromotionMapper mapper;

    public PromotionRepositoryAdapter(PromotionJpaRepository jpaRepository, PromotionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Promotion> trouverParId(Long id) {
        return jpaRepository.findById(id).map(mapper::versModele);
    }

    @Override
    public List<Promotion> listerToutes() {
        return jpaRepository.findAll().stream().map(mapper::versModele).toList();
    }
}
