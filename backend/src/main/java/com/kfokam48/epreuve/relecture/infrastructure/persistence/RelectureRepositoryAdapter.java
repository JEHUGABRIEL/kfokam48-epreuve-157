package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;

import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptateur : implémente le port du domaine au-dessus de Spring Data et du mapper. */
@Component
public class RelectureRepositoryAdapter implements RelectureRepository {

    private final RelectureJpaRepository jpaRepository;
    private final RelectureMapper mapper;

    public RelectureRepositoryAdapter(RelectureJpaRepository jpaRepository, RelectureMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Relecture enregistrer(Relecture relecture) {
        return mapper.versModele(jpaRepository.save(mapper.versEntite(relecture)));
    }

    @Override
    public Optional<Relecture> trouverParId(Long id) {
        return jpaRepository.findById(id).map(mapper::versModele);
    }

    @Override
    public Optional<Relecture> trouverParExerciceId(Long exerciceId) {
        return jpaRepository.findByExerciceId(exerciceId).map(mapper::versModele);
    }
}
