package com.kfokam48.epreuve.exercice.domain.model;

import com.kfokam48.epreuve.exercice.domain.LienInvalideException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test unitaire (B6) d'une règle métier réelle : la validité du lien d'exercice (EF3, 400
 * {@code LIEN_INVALIDE}).
 *
 * <p>Sans base ni contexte Spring : c'est précisément ce que permet un modèle de domaine qui ne connaît
 * pas la persistance.
 */
class ExerciceTest {

    @Test
    void accepte_un_lien_http_ou_https() {
        assertDoesNotThrow(() -> Exercice.verifierLien("https://github.com/JEHUGABRIEL/epreuve/blob/main/README.md"));
        assertDoesNotThrow(() -> Exercice.verifierLien("http://exemple.local/exercice.pdf"));
        assertDoesNotThrow(() -> Exercice.verifierLien("  https://exemple.local/exercice.pdf  "));
    }

    @Test
    void refuse_un_lien_vide() {
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien(null));
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien("   "));
    }

    @Test
    void refuse_un_lien_sans_schema_http_ou_sans_hote() {
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien("mon exercice"));
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien("ftp://exemple.local/x"));
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien("exercice.pdf"));
        assertThrows(LienInvalideException.class, () -> Exercice.verifierLien("https://"));
    }

    @Test
    void le_statut_initial_est_depose() {
        Exercice exercice = new Exercice();
        assertEquals(StatutExercice.DEPOSE, exercice.getStatut());
    }
}
