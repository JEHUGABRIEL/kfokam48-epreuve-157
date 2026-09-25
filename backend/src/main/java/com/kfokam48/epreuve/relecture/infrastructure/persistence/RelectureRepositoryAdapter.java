package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<Relecture> listerParRelecteurEtStatut(Long relecteurId, StatutRelecture statut) {
        return jpaRepository.findByRelecteurIdAndStatut(relecteurId, statut).stream()
                .map(mapper::versModele)
                .toList();
    }

    @Override
    public List<Relecture> listerParRelecteurEtStatut(Long relecteurId, StatutRelecture statut,
                                                      PageDemandee page) {
        return jpaRepository.findByRelecteurIdAndStatut(relecteurId, statut,
                        PageRequest.of(page.numero() - 1, page.taille(), Sort.by("id").ascending()))
                .stream()
                .map(mapper::versModele)
                .toList();
    }

    @Override
    public long compterParRelecteurEtStatut(Long relecteurId, StatutRelecture statut) {
        return jpaRepository.countByRelecteurIdAndStatut(relecteurId, statut);
    }

    @Override
    public Map<Long, Double> moyenneParAuteur(Collection<Long> etudiantIds) {
        Map<Long, Double> moyennes = new HashMap<>();
        if (etudiantIds.isEmpty()) {
            return moyennes;
        }
        for (Object[] ligne : jpaRepository.moyenneParAuteur(etudiantIds)) {
            // avg() renvoie un type dépendant du dialecte : on lit un Number, pas un Double supposé.
            moyennes.put((Long) ligne[0], ((Number) ligne[1]).doubleValue());
        }
        return moyennes;
    }

    @Override
    public Map<Long, Long> compterEnAttenteParRelecteur(Collection<Long> etudiantIds) {
        Map<Long, Long> comptes = new HashMap<>();
        if (etudiantIds.isEmpty()) {
            return comptes;
        }
        for (Object[] ligne : jpaRepository.compterParRelecteurEtStatut(etudiantIds, StatutRelecture.ASSIGNEE)) {
            comptes.put((Long) ligne[0], (Long) ligne[1]);
        }
        return comptes;
    }
}
