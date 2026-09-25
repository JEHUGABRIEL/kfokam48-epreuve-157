package com.kfokam48.epreuve.common.referentiel.domain;

import com.kfokam48.epreuve.common.referentiel.domain.model.Etudiant;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance du domaine : pas de Spring Data ici, l'implémentation vit en
 * {@code infrastructure/persistence/}.
 */
public interface EtudiantRepository {

    /** Sert à refuser un {@code etudiantId} reçu en entrée qui ne correspond à personne (ETUDIANT_INCONNU). */
    Optional<Etudiant> trouverParId(Long id);

    /** EF9 / RG14 : le tableau est construit promotion par promotion, à partir de ses étudiants. */
    List<Etudiant> listerParPromotion(Long promotionId);
}
