package com.kfokam48.epreuve.common.referentiel.application;

import com.kfokam48.epreuve.common.pagination.application.ResultatPage;
import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.common.referentiel.application.dto.EtudiantResponse;
import com.kfokam48.epreuve.common.referentiel.application.dto.PromotionResponse;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionInconnueException;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;
import com.kfokam48.epreuve.common.referentiel.domain.model.Promotion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Cas d'usage du référentiel : les listes qui permettent d'identifier l'appelant et la promotion.
 *
 * <p>Ces lectures sont ce qui rend les trois écrans possibles : sans elles, l'écran formateur ne peut
 * pas choisir de promotion pour {@code POST /api/sessions} (EF2, F2), et l'étudiant ne peut pas se
 * choisir dans une liste (Q1). Le contrat imposé ne les prévoyait pas — elles sont ajoutées et
 * documentées dans {@code api/contrat.yaml}.
 */
@Service
public class ReferentielService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    public ReferentielService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    /**
     * @param pagination vide si l'appel n'a demandé aucune pagination : le contrat imposé ne connaît
     *     pas ces paramètres, un appel sans eux doit rendre la collection entière.
     */
    @Transactional(readOnly = true)
    public ResultatPage<PromotionResponse> listerPromotions(Optional<PageDemandee> pagination) {
        List<Promotion> promotions;
        long total;
        if (pagination.isEmpty()) {
            promotions = promotionRepository.listerToutes();
            total = promotions.size();
        } else {
            promotions = promotionRepository.listerToutes(pagination.get());
            total = promotionRepository.compterToutes();
        }

        List<PromotionResponse> reponses = promotions.stream()
                .map(promotion -> new PromotionResponse(promotion.getId(), promotion.getNom()))
                .toList();
        return new ResultatPage<>(reponses, total);
    }

    /**
     * EF9 : le tableau est demandé par promotion, et l'opération impose un 404 si la promotion n'existe
     * pas. Une liste d'étudiants d'une promotion inconnue est le même cas — même décision, même code.
     */
    @Transactional(readOnly = true)
    public ResultatPage<EtudiantResponse> listerEtudiants(Long promotionId,
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
            total = etudiantRepository.compterParPromotion(promotionId);
        }

        List<EtudiantResponse> reponses = etudiants.stream()
                .map(etudiant -> new EtudiantResponse(etudiant.getId(), etudiant.getNom()))
                .toList();
        return new ResultatPage<>(reponses, total);
    }
}
