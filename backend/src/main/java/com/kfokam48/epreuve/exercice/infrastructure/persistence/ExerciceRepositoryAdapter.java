package com.kfokam48.epreuve.exercice.infrastructure.persistence;

import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Adaptateur : implémente le port du domaine au-dessus de Spring Data et du mapper. */
@Component
public class ExerciceRepositoryAdapter implements ExerciceRepository {

    private final ExerciceJpaRepository jpaRepository;
    private final ExerciceMapper mapper;

    public ExerciceRepositoryAdapter(ExerciceJpaRepository jpaRepository, ExerciceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Exercice enregistrer(Exercice exercice) {
        return mapper.versModele(jpaRepository.save(mapper.versEntite(exercice)));
    }

    @Override
    public Optional<Exercice> trouverParId(Long id) {
        return jpaRepository.findById(id).map(mapper::versModele);
    }

    @Override
    public Optional<Exercice> trouverParSessionEtEtudiant(Long sessionId, Long etudiantId) {
        return jpaRepository.findBySessionIdAndEtudiantId(sessionId, etudiantId).map(mapper::versModele);
    }

    @Override
    public Map<Long, Long> compterParEtudiant(Collection<Long> etudiantIds) {
        Map<Long, Long> comptes = new HashMap<>();
        if (etudiantIds.isEmpty()) {
            return comptes;
        }
        for (Object[] ligne : jpaRepository.compterParEtudiant(etudiantIds)) {
            comptes.put((Long) ligne[0], (Long) ligne[1]);
        }
        return comptes;
    }
}
