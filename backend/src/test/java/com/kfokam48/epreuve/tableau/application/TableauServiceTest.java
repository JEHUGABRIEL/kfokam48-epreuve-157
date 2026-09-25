package com.kfokam48.epreuve.tableau.application;

import com.kfokam48.epreuve.common.pagination.application.ResultatPage;
import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        assertThrows(PromotionInconnueException.class, () -> service.recapitulatif(999L, Optional.empty()));
    }

    @Test
    void agrege_les_quatre_indicateurs_par_etudiant() {
        when(promotions.trouverParId(1L)).thenReturn(Optional.of(new Promotion()));
        when(etudiants.listerParPromotion(1L)).thenReturn(List.of(etudiant(7L, "Binga"), etudiant(8L, "Sans rien")));

        when(presences.compterParEtudiant(any())).thenReturn(Map.of(7L, 3L));
        when(exercices.compterParEtudiant(any())).thenReturn(Map.of(7L, 2L));
        when(relectures.moyenneParAuteur(any())).thenReturn(Map.of(7L, 12.5));
        when(relectures.compterEnAttenteParRelecteur(any())).thenReturn(Map.of(7L, 1L));

        List<LigneTableauResponse> tableau = service.recapitulatif(1L, Optional.empty()).elements();

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

    @Test
    void sans_pagination_lit_toute_la_promotion_sans_requete_de_comptage() {
        when(promotions.trouverParId(1L)).thenReturn(Optional.of(new Promotion()));
        when(etudiants.listerParPromotion(1L)).thenReturn(List.of(etudiant(7L, "Binga")));

        ResultatPage<LigneTableauResponse> resultat = service.recapitulatif(1L, Optional.empty());

        assertEquals(1L, resultat.total(), "le total d'une lecture non paginée est ce qu'elle a lu");
        verify(etudiants).listerParPromotion(1L);
        verify(etudiants, never()).compterParPromotion(any());
    }

    @Test
    void ne_demande_les_agregats_que_pour_la_page_demandee() {
        when(promotions.trouverParId(1L)).thenReturn(Optional.of(new Promotion()));
        when(etudiants.listerParPromotion(eq(1L), any(PageDemandee.class)))
                .thenReturn(List.of(etudiant(7L, "Binga"), etudiant(8L, "Sans rien")));
        when(etudiants.compterParPromotion(1L)).thenReturn(60L);
        when(presences.compterParEtudiant(any())).thenReturn(Map.of(7L, 3L));
        when(exercices.compterParEtudiant(any())).thenReturn(Map.of(7L, 2L));
        when(relectures.moyenneParAuteur(any())).thenReturn(Map.of(7L, 12.5));
        when(relectures.compterEnAttenteParRelecteur(any())).thenReturn(Map.of(7L, 1L));

        ResultatPage<LigneTableauResponse> resultat =
                service.recapitulatif(1L, Optional.of(new PageDemandee(2, 20)));

        assertEquals(2, resultat.elements().size(), "une ligne par étudiant de la page");
        assertEquals(60L, resultat.total(), "le total est celui de la promotion, pas celui de la page");
        verify(etudiants).listerParPromotion(1L, new PageDemandee(2, 20));
        verify(etudiants, never()).listerParPromotion(1L);
        // ENF2 : les quatre agrégats ne portent que sur les étudiants de la page, jamais sur les 60.
        verify(presences).compterParEtudiant(List.of(7L, 8L));
    }

    private static Etudiant etudiant(Long id, String nom) {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(id);
        etudiant.setNom(nom);
        etudiant.setPromotionId(1L);
        return etudiant;
    }
}
