package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité JPA de la table {@code relecture} (cf. D2, {@code V1__schema_initial.sql} et
 * {@code V3__deux_relecteurs_par_exercice.sql}).
 *
 * <p>La base porte deux invariants que la classe ne fait que refléter : l'unicité
 * {@code (exercice_id, relecteur_id)} (RG4, révisée à l'étape 3) et les deux contraintes
 * {@code CHECK} — note entre 0 et 20 (RG3), et « statut RENDUE ⇒ note et date de rendu présentes »
 * (RG12).
 *
 * <p>La contrainte d'unicité est répétée ici, alors que Flyway la porte déjà en production : les
 * tests tournent sur une base H2 dont le schéma est dérivé des entités (Flyway y est désactivé pour
 * que {@code ./mvnw test} ne demande ni Docker ni PostgreSQL). Sans cette répétition, les tests
 * accepteraient deux relectures du même relecteur sur le même exercice, et ne pourraient pas
 * reproduire un schéma réel — c'est exactement le genre d'écart qui laisse passer un défaut.
 */
@Entity
@Table(name = "relecture",
        uniqueConstraints = @UniqueConstraint(name = "uq_relecture_exercice_relecteur",
                columnNames = {"exercice_id", "relecteur_id"}))
@Getter
@Setter
@NoArgsConstructor
public class RelectureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice_id", nullable = false)
    private Long exerciceId;

    @Column(name = "relecteur_id", nullable = false)
    private Long relecteurId;

    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRelecture statut;

    @Column(name = "assignee_at", nullable = false)
    private Instant assigneeAt;

    @Column(name = "rendu_at")
    private Instant renduAt;
}
