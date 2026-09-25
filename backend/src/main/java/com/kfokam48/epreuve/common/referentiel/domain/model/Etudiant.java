package com.kfokam48.epreuve.common.referentiel.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Étudiant (cf. D2). Il n'existe pas d'entité {@code Relecteur} : un relecteur est un étudiant
 * référencé par {@code Relecture.relecteurId} (RG5).
 *
 * <p>Donnée partagée : lue par session, presence, exercice, relecture et tableau. La placer dans un
 * module métier aurait créé une dépendance croisée artificielle entre modules.
 */
@Getter
@Setter
@NoArgsConstructor
public class Etudiant {

    private Long id;
    private String nom;
    private Long promotionId;
}
