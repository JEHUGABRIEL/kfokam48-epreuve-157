package com.kfokam48.epreuve.tableau.application;

import com.kfokam48.epreuve.common.pagination.application.ResultatPage;
import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionInconnueException;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.tableau.application.dto.LigneTableauResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Module transverse de restitution : pas de domaine propre, il lit les <em>ports</em> des autres
 * modules — jamais leur infrastructure, ce qui est la seule façon de rester découplé.
 *
 * <ul>
 *   <li>Tableau récapitulatif du formateur : EF9, RG14, Q16 — présences, exercices déposés, moyenne des
 *       notes reçues, relectures encore en attente, par étudiant.</li>
 * </ul>
 *
 * <p>La moyenne est calculée ici et uniquement ici : aucun calcul métier dupliqué côté front (F3). Le
 * front affiche ce que l'API lui donne, il ne recompte rien.
 *
 * <p>ENF2 (moins de 2 s pour 60 étudiants) est tenu par construction : quatre requêtes agrégées, une
 * par indicateur, et une seule lecture des étudiants. Aucun comptage dans une boucle.
 */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository,
                          EtudiantRepository etudiantRepository,
                          PresenceRepository presenceRepository,
                          ExerciceRepository exerciceRepository,
                          RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    /**
     * EF9, RG14 — le 404 sur promotion inconnue est imposé par le contrat.
     *
     * @param pagination vide si l'appel n'a rien demandé : la réponse est alors exactement celle du
     *     contrat imposé, la promotion entière. Sinon seuls les étudiants de la page sont lus, et les
     *     quatre agrégats ne portent que sur eux — ENF2 tient donc aussi page par page.
     */
    @Transactional(readOnly = true)
    public ResultatPage<LigneTableauResponse> recapitulatif(Long promotionId,
                                                            Optional<PageDemandee> pagination) {
        if (promotionRepository.trouverParId(promotionId).isEmpty()) {
            throw new PromotionInconnueException();
        }

        List<Etudiant> etudiants;
        long total;
        if (pagination.isEmpty()) {
            etudiants = etudiantRepository.listerParPromotion(promotionId);
            total = etudiants.size();
        } else {
            etudiants = etudiantRepository.listerParPromotion(promotionId, pagination.get());
            // Le total de la promotion, pas celui de la page : c'est lui qui donne le nombre de pages.
            total = etudiantRepository.compterParPromotion(promotionId);
        }

        if (etudiants.isEmpty()) {
            return new ResultatPage<>(List.of(), total);
        }

        List<Long> etudiantIds = etudiants.stream().map(Etudiant::getId).toList();

        Map<Long, Long> presences = presenceRepository.compterParEtudiant(etudiantIds);
        Map<Long, Long> exercices = exerciceRepository.compterParEtudiant(etudiantIds);
        Map<Long, Double> moyennes = relectureRepository.moyenneParAuteur(etudiantIds);
        Map<Long, Long> relecturesEnAttente = relectureRepository.compterEnAttenteParRelecteur(etudiantIds);

        List<LigneTableauResponse> lignes = etudiants.stream()
                .map(etudiant -> new LigneTableauResponse(
                        etudiant.getId(),
                        etudiant.getNom(),
                        presences.getOrDefault(etudiant.getId(), 0L),
                        exercices.getOrDefault(etudiant.getId(), 0L),
                        moyennes.get(etudiant.getId()),
                        relecturesEnAttente.getOrDefault(etudiant.getId(), 0L)))
                .toList();
        return new ResultatPage<>(lignes, total);
    }
}
