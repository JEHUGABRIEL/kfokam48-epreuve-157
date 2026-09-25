package com.kfokam48.epreuve.session.infrastructure.persistence;

import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptateur : implémente le port du domaine au-dessus de Spring Data et du mapper.
 *
 * <p>Il rend toujours un modèle du domaine, jamais une entité de stockage : l'identifiant généré par
 * la base est reporté sur le modèle, pour que l'appelant n'ait jamais à toucher à la persistance.
 */
@Component
public class SessionRepositoryAdapter implements SessionRepository {

    private final SessionJpaRepository jpaRepository;
    private final SessionMapper mapper;

    public SessionRepositoryAdapter(SessionJpaRepository jpaRepository, SessionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Session enregistrer(Session session) {
        SessionEntity enregistree = jpaRepository.save(mapper.versEntite(session));
        return mapper.versModele(enregistree);
    }

    @Override
    public Optional<Session> trouverParId(Long id) {
        return jpaRepository.findById(id).map(mapper::versModele);
    }

    @Override
    public Optional<Session> trouverParCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::versModele);
    }
}
