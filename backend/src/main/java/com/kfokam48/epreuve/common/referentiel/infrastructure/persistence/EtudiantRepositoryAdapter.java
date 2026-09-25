package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public List<Etudiant> listerParPromotion(Long promotionId, PageDemandee page) {
        // Le tri est porté par la requête : sans lui, l'ordre d'une page ne serait pas celui de la
        // lecture complète, et un élément pourrait apparaître deux fois ou jamais.
        return jpaRepository.findByPromotionId(promotionId,
                        PageRequest.of(page.numero() - 1, page.taille(), Sort.by("nom").ascending()))
                .stream()
                .map(mapper::versModele)
                .toList();
    }

    @Override
    public long compterParPromotion(Long promotionId) {
        return jpaRepository.countByPromotionId(promotionId);
    }
}
