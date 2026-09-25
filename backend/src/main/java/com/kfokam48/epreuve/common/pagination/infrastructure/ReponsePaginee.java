package com.kfokam48.epreuve.common.pagination.infrastructure;

import com.kfokam48.epreuve.common.pagination.application.ResultatPage;

import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Ce que le HTTP comprend de la pagination : un en-tête et rien d'autre.
 *
 * <p>Le corps reste un tableau JSON — sur {@code GET /api/tableau}, il est imposé par le sujet. Le total
 * est donc publié dans {@code X-Total-Count}, présent même sur une lecture non paginée, pour qu'un
 * client n'ait jamais deux formes de réponse à connaître.
 *
 * <p>Un appelant hors du navigateur (curl, tests) lit cet en-tête sans configuration. Une requête
 * cross-origin, elle, devrait l'exposer par {@code Access-Control-Expose-Headers} : ce projet n'en a pas
 * besoin, le frontend passant par le proxy de Vite, donc par la même origine.
 */
public final class ReponsePaginee {

    /** Nombre total d'éléments avant découpage. */
    public static final String EN_TETE_TOTAL = "X-Total-Count";

    private ReponsePaginee() {
    }

    public static <T> ResponseEntity<List<T>> de(ResultatPage<T> resultat) {
        return ResponseEntity.ok()
                .header(EN_TETE_TOTAL, Long.toString(resultat.total()))
                .body(resultat.elements());
    }
}
