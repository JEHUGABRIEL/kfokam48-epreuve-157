package com.kfokam48.epreuve.tableau.application;

/**
 * Module transverse de restitution (pas de domaine propre : il lit les repositories des autres
 * modules).
 * <ul>
 *   <li>Tableau récapitulatif du formateur : EF9, RG14 — présences, exercices déposés, moyenne des
 *       notes reçues, relectures en attente, par étudiant.</li>
 * </ul>
 * La moyenne est calculée ici et uniquement ici : aucun calcul métier dupliqué côté front (F3).
 * ENF2 : réponse en moins de 2 s pour une promotion de 60 étudiants.
 */
public class TableauService {
}
