package com.kfokam48.epreuve.relecture.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RG4 révisée (étape 3) : la note retenue d'un exercice est la moyenne de ses relectures, et elle
 * n'est définitive que lorsque les deux pairs ont rendu.
 *
 * <p>Testé ici, sans base ni contexte Spring, parce que c'est une règle de gestion — et parce que
 * c'est la règle que le client vient de changer. Une règle qui change mérite un test qui la fixe :
 * sans lui, rien ne distingue « c'est fait exprès » de « c'est un reste de l'ancien besoin ».
 */
class NoteRetenueTest {

    // Aucune note rendue : il n'y a rien à afficher, et ce n'est pas une erreur. « Pas encore de
    // note » n'est pas « zéro » — la distinction est celle que fait déjà le tableau du formateur.
    @Test
    void aucune_relecture_rendue_ne_donne_aucune_note() {
        assertThat(NoteRetenue.depuis(List.of(assignee(1L), assignee(2L)))).isEmpty();
    }

    @Test
    void une_seule_note_rendue_est_retenue_et_marquee_provisoire() {
        Optional<NoteRetenue> retenue = NoteRetenue.depuis(List.of(rendue(1L, 12), assignee(2L)));

        assertThat(retenue).isPresent();
        assertThat(retenue.get().note()).isEqualTo(12.0);
        assertThat(retenue.get().provisoire()).isTrue();
    }

    @Test
    void deux_notes_rendues_donnent_leur_moyenne_definitive() {
        Optional<NoteRetenue> retenue = NoteRetenue.depuis(List.of(rendue(1L, 12), rendue(2L, 15)));

        assertThat(retenue).isPresent();
        assertThat(retenue.get().note()).isEqualTo(13.5);
        assertThat(retenue.get().provisoire()).isFalse();
    }

    // La moyenne des deux notes est la moyenne, pas un arrangement : l'arrondi à l'entier aurait
    // transformé 13,5 en 13 ou 14, et le client a demandé une moyenne (§7).
    @Test
    void la_moyenne_nest_pas_arrondie_a_lentier() {
        Optional<NoteRetenue> retenue = NoteRetenue.depuis(List.of(rendue(1L, 13), rendue(2L, 14)));

        assertThat(retenue.orElseThrow().note()).isEqualTo(13.5);
    }

    // Le cas du §7 : un seul étudiant éligible comme pair. La note existe — mieux que pas de note —,
    // mais elle ne peut pas devenir définitive, puisqu'aucun second relecteur n'existe. Rester
    // provisoire pour toujours est la seule réponse honnête ; présenter cette note comme acquise ne
    // l'aurait pas été.
    @Test
    void une_relecture_unique_rendue_reste_provisoire() {
        Optional<NoteRetenue> retenue = NoteRetenue.depuis(List.of(rendue(1L, 9)));

        assertThat(retenue.orElseThrow().provisoire()).isTrue();
    }

    private Relecture rendue(Long id, int note) {
        Relecture relecture = assignee(id);
        relecture.rendre(note, "commentaire " + id);
        return relecture;
    }

    private Relecture assignee(Long id) {
        Relecture relecture = new Relecture();
        relecture.setId(id);
        relecture.setExerciceId(10L);
        relecture.setRelecteurId(id);
        relecture.setStatut(StatutRelecture.ASSIGNEE);
        return relecture;
    }
}
