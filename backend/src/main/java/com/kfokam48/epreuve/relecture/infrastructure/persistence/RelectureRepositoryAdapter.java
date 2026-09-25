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
    public List<Relecture> trouverParExerciceId(Long exerciceId) {
        return jpaRepository.findByExerciceId(exerciceId).stream()
                .map(mapper::versModele)
                .toList();
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

        // La requête rend une moyenne **par exercice**. Il en faut une par auteur, sinon un exercice
        // relu par deux pairs pèserait deux fois plus lourd qu'un exercice relu par un seul : la
        // moyenne de l'étudiant porte sur les notes retenues, pas sur toutes les notes reçues (RG14,
        // révisée à l'étape 3). Le cumul se fait ici, sur au plus deux lignes par exercice.
        Map<Long, double[]> cumuls = new HashMap<>();
        for (Object[] ligne : jpaRepository.moyenneParExerciceEtAuteur(etudiantIds)) {
            double[] cumul = cumuls.computeIfAbsent((Long) ligne[0], auteur -> new double[2]);
            // avg() renvoie un type dépendant du dialecte : on lit un Number, pas un Double supposé.
            cumul[0] += ((Number) ligne[2]).doubleValue();
            cumul[1] += 1;
        }

        cumuls.forEach((auteur, cumul) -> moyennes.put(auteur, cumul[0] / cumul[1]));
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
