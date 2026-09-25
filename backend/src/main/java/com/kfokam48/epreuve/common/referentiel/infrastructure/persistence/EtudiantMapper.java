package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;

import org.springframework.stereotype.Component;

/** Traduit entre le modèle du domaine et l'entité de stockage (mapping manuel, champ par champ). */
@Component
public class EtudiantMapper {

    public Etudiant versModele(EtudiantEntity entite) {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(entite.getId());
        etudiant.setNom(entite.getNom());
        etudiant.setPromotionId(entite.getPromotionId());
        return etudiant;
    }
}
