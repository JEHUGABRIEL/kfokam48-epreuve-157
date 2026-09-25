package com.kfokam48.epreuve.common.pagination.application;

import java.util.List;

/**
 * Une tranche de résultats, et le total qu'elle découpe.
 *
 * <p>Le total voyage à côté du corps et non dedans : la réponse de {@code GET /api/tableau} est un
 * tableau JSON imposé par le sujet, elle ne peut pas devenir une enveloppe
 * {@code { elements, total }}. Le contrôleur le publie donc dans l'en-tête {@code X-Total-Count}.
 */
public record ResultatPage<T>(List<T> elements, long total) {

    /** Lecture non paginée : le total est celui de ce qu'on vient de lire, sans requête de comptage. */
    public static <T> ResultatPage<T> deTout(List<T> elements) {
        return new ResultatPage<>(elements, elements.size());
    }
}
