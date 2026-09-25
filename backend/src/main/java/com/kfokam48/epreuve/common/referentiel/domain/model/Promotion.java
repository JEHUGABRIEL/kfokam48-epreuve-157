package com.kfokam48.epreuve.common.referentiel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Promotion (cf. D2) : regroupe des étudiants et des sessions.
 *
 * <p>Modèle du domaine, sans aucune annotation de persistance : la table correspondante est décrite par
 * {@code PromotionEntity}, dans {@code infrastructure/persistence/}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Promotion {

    private Long id;
    private String nom;
}
