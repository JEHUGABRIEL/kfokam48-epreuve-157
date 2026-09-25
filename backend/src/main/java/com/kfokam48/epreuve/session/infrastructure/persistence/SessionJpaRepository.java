package com.kfokam48.epreuve.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Dépôt Spring Data : c'est un détail de stockage. Il est injecté par l'adaptateur uniquement, jamais
 * par la couche application — sinon le domaine dépendrait de Spring Data par la bande.
 */
public interface SessionJpaRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findByCode(String code);
}
