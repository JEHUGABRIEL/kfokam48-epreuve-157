package com.kfokam48.epreuve.presence.domain.model;

/**
 * Origine d'une présence (cf. D2, RG11). Le contrat expose exactement ces deux valeurs, et Q14 exige
 * que la présence ajoutée à la main se voie : c'est ce champ qui la distingue.
 */
public enum SourcePresence {
    ETUDIANT,
    FORMATEUR
}
