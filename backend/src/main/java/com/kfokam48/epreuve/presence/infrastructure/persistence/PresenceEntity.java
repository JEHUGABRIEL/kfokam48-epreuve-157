package com.kfokam48.epreuve.presence.infrastructure.persistence;

import com.kfokam48.epreuve.presence.domain.model.SourcePresence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité JPA de la table {@code presence} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>La contrainte d'unicité {@code (session_id, etudiant_id)} appartient au schéma (ENF4), pas à
 * cette classe : c'est la base qui garantit qu'une double saisie simultanée ne passe pas.
 */
@Entity
@Table(name = "presence")
@Getter
@Setter
@NoArgsConstructor
public class PresenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(nullable = false)
    private Instant horodatage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourcePresence source;
}
