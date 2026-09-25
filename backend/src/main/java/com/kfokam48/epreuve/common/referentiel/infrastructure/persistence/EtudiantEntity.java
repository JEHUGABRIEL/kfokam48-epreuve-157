package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA de la table {@code etudiant} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>La promotion est portée par une simple colonne {@code promotion_id}, pas par une association
 * JPA : le domaine ne manipule que des identifiants, et une association ouvrirait la porte aux
 * chargements paresseux hors transaction ({@code open-in-view} est désactivé).
 */
@Entity
@Table(name = "etudiant")
@Getter
@Setter
@NoArgsConstructor
public class EtudiantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;
}
