package com.kfokam48.epreuve.tableau.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionInconnueException;
import com.kfokam48.epreuve.common.referentiel.domain.PromotionRepository;
import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;
import com.kfokam48.epreuve.common.referentiel.domain.model.Promotion;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.tableau.application.dto.LigneTableauResponse;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test unitaire (B6) de l'agrégation du tableau : EF9, RG14 et ENF2.
 *
 * <p>Ce que le test protège n'est pas l'arithmétique — la moyenne est calculée par la base — mais la
 * composition : les quatre indicateurs sont lus en quatre requêtes groupées, et un étudiant sans note
 * reçue garde une moyenne {@code null} au lieu d'un zéro trompeur.
 */
class TableauServiceTest {

    private final PromotionRepository promotions = mock(PromotionRepository.class);
    private final EtudiantRepository etudiants = mock(EtudiantRepository.class);
    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final ExerciceRepository exercices = mock(ExerciceRepository.class);
    private final RelectureRepository relectures = mock(RelectureRepository.class);

    private final TableauService service =
            new TableauService(promotions, etudiants, presences, exercices, relectures);

    @Test
    void refuse_une_promotion_inconnue() {
        when(promotions.trouverParId(999L)).thenReturn(Optional.empty());

        assertThrows(PromotionInconnueException.class, () -> service.recapitulatif(999L));
    }

    @Test
    void agrege_les_quatre_indicateurs_par_etudiant() {
        when(promotions.trouverParId(1L)).thenReturn(Optional.of(new Promotion()));
        when(etudiants.listerParPromotion(1L)).thenReturn(List.of(etudiant(7L, "Binga"), etudiant(8L, "Sans rien")));

        when(presences.compterParEtudiant(any())).thenReturn(Map.of(7L, 3L));
        when(exercices.compterParEtudiant(any())).thenReturn(Map.of(7L, 2L));
        when(relectures.moyenneParAuteur(any())).thenReturn(Map.of(7L, 12.5));
        when(relectures.compterEnAttenteParRelecteur(any())).thenReturn(Map.of(7L, 1L));

        List<LigneTableauResponse> tableau = service.recapitulatif(1L);

        assertEquals(2, tableau.size(), "une ligne par étudiant de la promotion");

        LigneTableauResponse premier = tableau.get(0);
        assertEquals(7L, premier.etudiantId());
        assertEquals("Binga", premier.nom());
        assertEquals(3L, premier.presences());
        assertEquals(2L, premier.exercicesDeposes());
        assertEquals(12.5, premier.moyenne());
        assertEquals(1L, premier.relecturesEnAttente());

        LigneTableauResponse second = tableau.get(1);
        assertEquals(0L, second.presences(), "aucune donnée ⇒ 0, pas null");
        assertEquals(0L, second.relecturesEnAttente());
        assertNull(second.moyenne(), "aucune note reçue ⇒ moyenne absente, et non 0");
    }

    private static Etudiant etudiant(Long id, String nom) {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(id);
        etudiant.setNom(nom);
        etudiant.setPromotionId(1L);
        return etudiant;
    }
}
