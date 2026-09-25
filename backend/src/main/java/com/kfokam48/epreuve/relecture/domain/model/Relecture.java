package com.kfokam48.epreuve.relecture.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Relecture attribuée à un étudiant pour un exercice donné (cf. D2).
 * <ul>
 *   <li>RG4 : un exercice n'a qu'un seul relecteur.</li>
 *   <li>RG6 : l'identité du relecteur ne sort jamais de l'API.</li>
 * </ul>
 *
 * <p>Il n'existe pas d'entité {@code Relecteur} : le relecteur est un {@code Etudiant} désigné par
 * {@code relecteurId}, tiré au sort à l'instant du dépôt (RG5).
 *
 * <p>Aucune annotation de persistance ici : la table est décrite par {@code RelectureEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Relecture {

    private Long id;
    private Long exerciceId;
    private Long relecteurId;
    private Integer note;
    private String commentaire;
    private StatutRelecture statut = StatutRelecture.ASSIGNEE;
    private Instant assigneeAt;
    private Instant renduAt;

    /** RG12 : une relecture rendue est définitive — c'est cet état qui verrouille le lien (RG10). */
    public boolean estRendue() {
        return statut == StatutRelecture.RENDUE;
    }
}
