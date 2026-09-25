package com.kfokam48.epreuve.common.pagination.domain;

import com.kfokam48.epreuve.common.error.RequeteInvalideException;

import java.util.Optional;

/**
 * Demande de découpage d'une lecture.
 *
 * <p><strong>La pagination est une extension, jamais un changement de comportement.</strong>
 * {@code GET /api/tableau} est une opération imposée par le sujet, sans pagination : un appel qui ne
 * fournit ni {@code page} ni {@code taille} doit rendre exactement ce qu'il rendait avant, c'est-à-dire
 * la collection entière. {@link #depuis(Integer, Integer)} rend donc {@code Optional.empty()} dans ce
 * cas, et un défaut de 20 lignes ne s'applique jamais à un appel qui n'a rien demandé.
 *
 * <p>Défauts quand un seul des deux paramètres est fourni : {@code page = 1} et {@code taille = 20}.
 * Bornes refusées en {@code 400 REQUETE_INVALIDE} — le contrat ne nomme aucun code pour une valeur hors
 * bornes (cf. cahier des charges §7).
 *
 * <p>Le nom est volontairement explicite : Spring Data possède déjà un {@code Page} et un
 * {@code Pageable}, que les adaptateurs emploient — deux types homonymes qui ne veulent pas dire la
 * même chose dans le même fichier seraient une source d'erreur silencieuse.
 */
public record PageDemandee(int numero, int taille) {

    public static final int TAILLE_PAR_DEFAUT = 20;

    /** Plafond de taille : au-delà, la « pagination » n'en est plus une et redevient une lecture totale. */
    public static final int TAILLE_MAXIMALE = 100;

    public PageDemandee {
        if (numero < 1) {
            throw new RequeteInvalideException("Le paramètre « page » doit être supérieur ou égal à 1.");
        }
        if (taille < 1 || taille > TAILLE_MAXIMALE) {
            throw new RequeteInvalideException(
                    "Le paramètre « taille » doit être compris entre 1 et " + TAILLE_MAXIMALE + ".");
        }
    }

    /**
     * Traduit les deux paramètres HTTP optionnels en demande de pagination.
     *
     * @return {@code Optional.empty()} si l'appel n'a demandé aucune pagination — c'est le cas du
     *     contrat imposé, qui ne connaît pas ces paramètres.
     */
    public static Optional<PageDemandee> depuis(Integer numero, Integer taille) {
        if (numero == null && taille == null) {
            return Optional.empty();
        }
        return Optional.of(new PageDemandee(numero == null ? 1 : numero,
                taille == null ? TAILLE_PAR_DEFAUT : taille));
    }

    /** Index du premier élément de la page, dans une collection triée. */
    public int premierIndex() {
        return (numero - 1) * taille;
    }
}
