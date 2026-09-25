package com.kfokam48.epreuve.session.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Session de cours : le modèle du domaine, avec ses invariants et ses règles (RG1, RG13).
 *
 * <p>Aucune annotation et aucune dépendance de persistance ici, volontairement : ce que le stockage
 * fait de cet objet ne regarde pas le domaine. La table correspondante est décrite par
 * {@code SessionEntity}, dans {@code infrastructure/persistence/}, et {@code SessionMapper} est le
 * seul endroit qui connaît les deux.
 */
@Getter
@Setter
@NoArgsConstructor
public class Session {

    private Long id;
    private String titre;
    private Long promotionId;
    private String code;
    private Instant ouvertureAt;
    private Instant expirationAt;
    private Instant clotureAt;
    private StatutSession statut = StatutSession.OUVERTE;

    // RG1 : le code n'est valable que 15 minutes — la validité se lit sur la session, pas sur le code.
    public boolean estExpiree() {
        return Instant.now().isAfter(expirationAt);
    }

    // RG13 : la clôture est un état, pas une date à comparer aux yeux de chaque appelant.
    public boolean estCloturee() {
        return statut == StatutSession.CLOTUREE;
    }
}
