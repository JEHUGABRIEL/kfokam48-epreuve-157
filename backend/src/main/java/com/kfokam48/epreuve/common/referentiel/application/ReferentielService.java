package com.kfokam48.epreuve.common.referentiel.application;

import com.kfokam48.epreuve.common.referentiel.application.dto.EtudiantResponse;
import com.kfokam48.epreuve.common.referentiel.application.dto.PromotionResponse;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionInconnueException;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<PromotionResponse> listerPromotions() {
        return promotionRepository.listerToutes().stream()
                .map(promotion -> new PromotionResponse(promotion.getId(), promotion.getNom()))
                .toList();
    }

    /**
     * EF9 : le tableau est demandé par promotion, et l'opération impose un 404 si la promotion n'existe
     * pas. Une liste d'étudiants d'une promotion inconnue est le même cas — même décision, même code.
     */
    @Transactional(readOnly = true)
    public List<EtudiantResponse> listerEtudiants(Long promotionId) {
        if (promotionRepository.trouverParId(promotionId).isEmpty()) {
            throw new PromotionInconnueException();
        }
        return etudiantRepository.listerParPromotion(promotionId).stream()
                .map(etudiant -> new EtudiantResponse(etudiant.getId(), etudiant.getNom()))
                .toList();
    }
}
