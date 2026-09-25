package com.kfokam48.epreuve.relecture.infrastructure.persistence;

import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité JPA de la table {@code relecture} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>La base porte deux invariants que la classe ne fait que refléter : l'unicité
 * {@code exercice_id} (RG4) et les deux contraintes {@code CHECK} — note entre 0 et 20 (RG3), et
 * « statut RENDUE ⇒ note et date de rendu présentes » (RG12).
 */
@Entity
@Table(name = "relecture")
@Getter
@Setter
@NoArgsConstructor
public class RelectureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice_id", nullable = false, unique = true)
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
