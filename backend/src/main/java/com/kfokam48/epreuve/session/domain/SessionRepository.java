package com.kfokam48.epreuve.session.domain;

import com.kfokam48.epreuve.session.domain.model.Session;

import java.util.Optional;

/**
 * Port de persistance du domaine : ce dont le domaine a besoin, dit dans son langage.
 *
 * <p>Cette interface n'étend volontairement pas {@code JpaRepository} : le domaine ne dépend ni de
 * Spring Data ni de JPA. L'implémentation vit dans {@code infrastructure/persistence/} — c'est ce qui
 * permet de tester les règles métier sans base de données et sans contexte Spring.
 */
public interface SessionRepository {

    Session enregistrer(Session session);

    Optional<Session> trouverParId(Long id);

    /** Utilisé par le module presence : c'est le code saisi qui désigne la session visée (EF1). */
    Optional<Session> trouverParCode(String code);
}
