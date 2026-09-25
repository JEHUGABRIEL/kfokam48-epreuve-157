package com.kfokam48.epreuve.relecture.application;

import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.presence.domain.model.SourcePresence;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;
import com.kfokam48.epreuve.session.domain.SessionRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitaire (B6) des règles de gestion RG2 et RG5 — un étudiant ne relit jamais son propre
 * exercice, et le relecteur est tiré parmi les présents.
 *
 * <p>Pas de contexte Spring : le tirage au sort est une interface, donc le test peut décider qui gagne
 * et vérifier qui était éligible.
 */
class RelectureServiceTest {

    private final RelectureRepository relectures = mock(RelectureRepository.class);
    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final TirageAuSort tirage = mock(TirageAuSort.class);
    private final RelectureService service = new RelectureService(
            relectures, presences, mock(ExerciceRepository.class), mock(SessionRepository.class), tirage);

    @Test
    void lauteur_nest_jamais_dans_les_candidats() {
        // L'auteur (1) est présent, comme souvent : il ne doit pourtant jamais être tiré (RG2, Q5).
        presencesDeLaSession(1L, 2L, 3L);
        when(tirage.tirer(anyList())).thenReturn(3L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        service.assigner(10L, 1L, 1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> candidats = ArgumentCaptor.forClass(List.class);
        verify(tirage).tirer(candidats.capture());
        assertEquals(List.of(2L, 3L), candidats.getValue(), "l'auteur figure parmi les candidats");
    }

    @Test
    void enregistre_la_relecture_assignee_au_relecteur_tire() {
        presencesDeLaSession(1L, 2L);
        when(tirage.tirer(anyList())).thenReturn(2L);
        when(relectures.enregistrer(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        assertTrue(assignation.isPresent());
        assertEquals(2L, assignation.get().getRelecteurId());
        assertEquals(10L, assignation.get().getExerciceId());
        assertEquals(StatutRelecture.ASSIGNEE, assignation.get().getStatut());
    }

    @Test
    void ne_cree_aucune_relecture_si_personne_dautre_nest_present() {
        // Seul l'auteur est présent : aucun candidat éligible. L'exercice reste sans relecture plutôt
        // que d'être attribué à son propre auteur (§7).
        presencesDeLaSession(1L);

        Optional<Relecture> assignation = service.assigner(10L, 1L, 1L);

        assertFalse(assignation.isPresent());
        verify(relectures, never()).enregistrer(any(Relecture.class));
    }

    private void presencesDeLaSession(Long... etudiantIds) {
        List<Presence> releve = java.util.Arrays.stream(etudiantIds).map(id -> {
            Presence presence = new Presence();
            presence.setEtudiantId(id);
            presence.setSessionId(1L);
            presence.setHorodatage(Instant.now());
            presence.setSource(SourcePresence.ETUDIANT);
            return presence;
        }).toList();
        when(presences.trouverParSession(1L)).thenReturn(releve);
    }
}
