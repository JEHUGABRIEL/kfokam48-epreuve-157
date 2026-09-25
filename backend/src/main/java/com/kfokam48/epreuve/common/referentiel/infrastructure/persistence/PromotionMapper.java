package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import com.kfokam48.epreuve.common.referentiel.domain.model.Promotion;

import org.springframework.stereotype.Component;

/**
 * Traduit entre le modèle du domaine et l'entité de stockage. Seul endroit qui connaît les deux.
 *
 * <p>Mapping écrit à la main, champ par champ : un champ oublié se verrait ici, là où une conversion
 * automatique le perdrait en silence.
 */
@Component
public class PromotionMapper {

    public Promotion versModele(PromotionEntity entite) {
        Promotion promotion = new Promotion();
        promotion.setId(entite.getId());
        promotion.setNom(entite.getNom());
        return promotion;
    }
}
