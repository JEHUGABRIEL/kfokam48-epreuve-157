package com.kfokam48.epreuve.exercice.infrastructure.persistence;

import com.kfokam48.epreuve.exercice.domain.model.StatutExercice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité JPA de la table {@code exercice} (cf. D2 et {@code V1__schema_initial.sql}).
 *
 * <p>La contrainte d'unicité {@code (session_id, etudiant_id)} appartient au schéma : c'est la base qui
 * garantit qu'un double dépôt simultané ne passe pas.
 */
@Entity
@Table(name = "exercice")
@Getter
@Setter
@NoArgsConstructor
public class ExerciceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;
}
