package com.kfokam48.epreuve.tableau.application.dto;

/**
 * Une ligne du tableau récapitulatif — champs requis : {@code etudiantId}, {@code nom},
 * {@code presences}, {@code exercicesDeposes}, {@code moyenne} (nullable), {@code relecturesEnAttente}.
 *
 * <p>{@code moyenne} est nullable et le reste : un étudiant sans note reçue n'a pas une moyenne de zéro,
 * il n'en a pas (Q16). Confondre les deux ferait paraître un étudiant qui n'a rien rendu comme un
 * étudiant qui a tout raté.
 */
public record LigneTableauResponse(Long etudiantId,
                                   String nom,
                                   long presences,
                                   long exercicesDeposes,
                                   Double moyenne,
                                   long relecturesEnAttente) {
}
