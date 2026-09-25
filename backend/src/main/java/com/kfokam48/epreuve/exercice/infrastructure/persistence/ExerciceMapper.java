package com.kfokam48.epreuve.exercice.infrastructure.persistence;

import com.kfokam48.epreuve.exercice.domain.model.Exercice;

import org.springframework.stereotype.Component;

/** Traduit entre le modèle du domaine et l'entité de stockage (mapping manuel, champ par champ). */
@Component
public class ExerciceMapper {

    public Exercice versModele(ExerciceEntity entite) {
        Exercice exercice = new Exercice();
        exercice.setId(entite.getId());
        exercice.setSessionId(entite.getSessionId());
        exercice.setEtudiantId(entite.getEtudiantId());
        exercice.setLien(entite.getLien());
        exercice.setStatut(entite.getStatut());
        exercice.setDeposeAt(entite.getDeposeAt());
        return exercice;
    }

    public ExerciceEntity versEntite(Exercice exercice) {
        ExerciceEntity entite = new ExerciceEntity();
        entite.setId(exercice.getId());
        entite.setSessionId(exercice.getSessionId());
        entite.setEtudiantId(exercice.getEtudiantId());
        entite.setLien(exercice.getLien());
        entite.setStatut(exercice.getStatut());
        entite.setDeposeAt(exercice.getDeposeAt());
        return entite;
    }
}
