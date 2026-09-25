package com.kfokam48.epreuve.presence.infrastructure.persistence;

import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Implémente le port du domaine au-dessus de Spring Data et du mapper. */
@Component
public class PresenceRepositoryAdapter implements PresenceRepository {

    private final PresenceJpaRepository jpaRepository;
    private final PresenceMapper mapper;

    public PresenceRepositoryAdapter(PresenceJpaRepository jpaRepository, PresenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Presence enregistrer(Presence presence) {
        return mapper.versModele(jpaRepository.save(mapper.versEntite(presence)));
    }

    @Override
    public Optional<Presence> trouverParSessionEtEtudiant(Long sessionId, Long etudiantId) {
        return jpaRepository.findBySessionIdAndEtudiantId(sessionId, etudiantId).map(mapper::versModele);
    }

    @Override
    public List<Presence> trouverParSession(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId).stream().map(mapper::versModele).toList();
    }
}
