package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.relecture.domain.model.Relecture;

import org.springframework.stereotype.Component;

/** Traduit entre le modèle du domaine et l'entité de stockage (mapping manuel, champ par champ). */
@Component
public class RelectureMapper {

    public Relecture versModele(RelectureEntity entite) {
        Relecture relecture = new Relecture();
        relecture.setId(entite.getId());
        relecture.setExerciceId(entite.getExerciceId());
        relecture.setRelecteurId(entite.getRelecteurId());
        relecture.setNote(entite.getNote());
        relecture.setCommentaire(entite.getCommentaire());
        relecture.setStatut(entite.getStatut());
        relecture.setAssigneeAt(entite.getAssigneeAt());
        relecture.setRenduAt(entite.getRenduAt());
        return relecture;
    }

    public RelectureEntity versEntite(Relecture relecture) {
        RelectureEntity entite = new RelectureEntity();
        entite.setId(relecture.getId());
        entite.setExerciceId(relecture.getExerciceId());
        entite.setRelecteurId(relecture.getRelecteurId());
        entite.setNote(relecture.getNote());
        entite.setCommentaire(relecture.getCommentaire());
        entite.setStatut(relecture.getStatut());
        entite.setAssigneeAt(relecture.getAssigneeAt());
        entite.setRenduAt(relecture.getRenduAt());
        return entite;
    }
}
