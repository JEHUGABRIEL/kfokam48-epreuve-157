package com.kfokam48.epreuve.relecture.application.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test unitaire (B6) de RG6 : l'étudiant relu voit sa note et le commentaire, jamais l'identité du
 * relecteur (Q8).
 *
 * <p>Le test porte sur la <em>structure</em> du DTO plutôt que sur une réponse HTTP : c'est la forme du
 * contrat qui garantit la règle. Une entité JPA exposée en JSON aurait laissé fuir
 * {@code relecteurId} par accident, et ce test serait alors tombé.
 */
class RelectureResponseTest {

    @Test
    void le_dto_ne_transporte_aucune_identite_de_relecteur() {
        List<String> champs = Arrays.stream(RelectureResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertEquals(List.of("statut", "note", "commentaire"), champs);
        assertFalse(RelectureResponse.class.getRecordComponents().length == 0);
        for (RecordComponent composant : RelectureResponse.class.getRecordComponents()) {
            assertFalse(composant.getName().toLowerCase().contains("relecteur"),
                    "un champ désignant le relecteur violerait RG6 : " + composant.getName());
        }
    }

    /** Même garantie pour ce que le relecteur reçoit : ni auteur, ni identité de qui que ce soit. */
    @Test
    void le_dto_du_relecteur_nexpose_ni_auteur_ni_relecteur() {
        for (RecordComponent composant : RelectureAssigneeResponse.class.getRecordComponents()) {
            String nom = composant.getName().toLowerCase();
            assertFalse(nom.contains("relecteur") || nom.contains("auteur") || nom.contains("etudiant"),
                    "l'identité ne doit pas sortir : " + composant.getName());
        }
    }
}
