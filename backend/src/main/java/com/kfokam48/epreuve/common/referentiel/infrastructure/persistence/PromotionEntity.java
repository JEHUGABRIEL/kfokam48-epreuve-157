package com.kfokam48.epreuve.common.referentiel.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA de la table {@code promotion} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>Elle porte ce que le modèle du domaine refuse de porter : les annotations et les noms de colonnes
 * en {@code snake_case}, pour que Hibernate valide son mapping contre le schéma Flyway
 * ({@code ddl-auto: validate} hors tests).
 */
@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;
}
