package com.kfokam48.epreuve.session.infrastructure.persistence;

import com.kfokam48.epreuve.session.domain.model.StatutSession;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité JPA de la table {@code session} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>Elle porte tout ce que le modèle du domaine refuse de porter : les annotations, les noms de
 * colonnes en {@code snake_case}, et le fait que le statut soit stocké en texte. Le passage entre les
 * deux est le travail de {@link SessionMapper} — et de lui seul.
 *
 * <p>Les colonnes sont nommées explicitement pour que Hibernate valide son mapping contre le schéma
 * Flyway ({@code ddl-auto: validate} en dev).
 */
@Entity
@Table(name = "session")
@Getter
@Setter
@NoArgsConstructor
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(name = "cloture_at")
    private Instant clotureAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSession statut;
}
