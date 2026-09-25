package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptateur : implémente le port du domaine au-dessus de Spring Data et du mapper. */
@Component
public class EtudiantRepositoryAdapter implements EtudiantRepository {

    private final EtudiantJpaRepository jpaRepository;
    private final EtudiantMapper mapper;

    public EtudiantRepositoryAdapter(EtudiantJpaRepository jpaRepository, EtudiantMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Etudiant> trouverParId(Long id) {
        return jpaRepository.findById(id).map(mapper::versModele);
    }

    @Override
    public List<Etudiant> listerParPromotion(Long promotionId) {
        return jpaRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(mapper::versModele)
                .toList();
    }
}
