package com.kfokam48.epreuve.exercice.domain.model;

import com.kfokam48.epreuve.exercice.domain.LienInvalideException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.time.Instant;

/**
 * Exercice déposé par un étudiant pour une session (cf. D2).
 * <ul>
 *   <li>RG9 : dépôt possible jusqu'à la clôture de la session par le formateur, indépendamment de
 *       l'heure de fin théorique.</li>
 *   <li>RG10 : le lien peut être remplacé tant que la relecture n'est pas rendue.</li>
 * </ul>
 *
 * <p>Le dépôt n'exige pas d'avoir marqué sa présence : c'est le compromis assumé et documenté en §7 du
 * cahier des charges — l'absent du jour peut rendre son travail, ce qui est le but affiché par Q12.
 *
 * <p>Aucune annotation de persistance ici : la table est décrite par {@code ExerciceEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Exercice {

    private Long id;
    private Long sessionId;
    private Long etudiantId;
    private String lien;
    private StatutExercice statut = StatutExercice.DEPOSE;
    private Instant deposeAt;

    /**
     * Valide le lien reçu (EF3). Le contrat impose un 400 {@code LIEN_INVALIDE} : la règle vit donc ici,
     * dans le domaine, et se teste sans base ni contexte Spring.
     *
     * <p>Un lien est valide s'il désigne une ressource HTTP(S) absolue : un texte quelconque, un chemin
     * relatif ou un schéma exotique ne sont pas des liens d'exercice, et un dépôt qu'on ne peut pas
     * ouvrir ne rend service à personne.
     */
    public static void verifierLien(String lien) {
        if (lien == null || lien.isBlank()) {
            throw new LienInvalideException();
        }
        String candidat = lien.trim();
        if (!candidat.matches("(?i)^https?://\\S+$")) {
            throw new LienInvalideException();
        }
        try {
            // Second filtre : la forme doit tenir debout aux yeux de java.net.URI (hôte présent).
            URI uri = URI.create(candidat);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new LienInvalideException();
            }
        } catch (IllegalArgumentException formeInvalide) {
            throw new LienInvalideException();
        }
    }

    /** EF5 : un relecteur a été assigné, l'exercice attend son rendu (cf. D4). */
    public void marquerEnAttenteDeRelecture() {
        this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
    }

    /** EF6 : la relecture est rendue, l'exercice est clos (cf. D4). */
    public void marquerRelu() {
        this.statut = StatutExercice.RELU;
    }
}
