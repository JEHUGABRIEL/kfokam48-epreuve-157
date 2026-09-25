package com.kfokam48.epreuve.relecture.domain.model;

import com.kfokam48.epreuve.relecture.domain.NoteInvalideException;
import com.kfokam48.epreuve.relecture.domain.RelectureDejaRendueException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire (B6) des deux règles de gestion du rendu : RG3 (note entière de 0 à 20, Q9) et RG12
 * (une relecture rendue est définitive, Q15).
 *
 * <p>Ni base ni contexte Spring : les règles vivent dans le modèle du domaine.
 */
class RelectureTest {

    @Test
    void accepte_les_deux_bornes_de_la_note() {
        Relecture zero = new Relecture();
        zero.rendre(0, "Rien à dire.");
        assertEquals(0, zero.getNote());

        Relecture vingt = new Relecture();
        vingt.rendre(20, "Parfait.");
        assertEquals(20, vingt.getNote());
    }

    @Test
    void refuse_une_note_hors_bornes_ou_absente() {
        assertThrows(NoteInvalideException.class, () -> new Relecture().rendre(-1, "Négatif."));
        assertThrows(NoteInvalideException.class, () -> new Relecture().rendre(21, "Trop haut."));
        assertThrows(NoteInvalideException.class, () -> new Relecture().rendre(null, "Absente."));
    }

    @Test
    void une_relecture_rendue_ne_se_modifie_plus() {
        Relecture relecture = new Relecture();
        assertFalse(relecture.estRendue());

        relecture.rendre(12, "Correct.");
        assertTrue(relecture.estRendue());
        assertNotNull(relecture.getRenduAt());

        // RG12 : le client a tranché Q15 contre Q10 — « une fois que le relecteur a validé, c'est fini ».
        assertThrows(RelectureDejaRendueException.class, () -> relecture.rendre(18, "Je me ravise."));
        assertEquals(12, relecture.getNote(), "la première note doit rester la seule");
    }
}
