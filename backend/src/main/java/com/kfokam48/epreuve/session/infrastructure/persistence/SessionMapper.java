package com.kfokam48.epreuve.session.infrastructure.persistence;

import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Component;

/**
 * Traduit entre le modèle du domaine et l'entité de stockage. C'est le seul endroit qui connaît les
 * deux, et c'est précisément ce qui évite au domaine de porter des annotations de persistance.
 *
 * <p>Le mapping est écrit à la main, champ par champ : un champ oublié se verrait ici, alors qu'une
 * conversion automatique le perdrait en silence.
 */
@Component
public class SessionMapper {

    public Session versModele(SessionEntity entite) {
        Session session = new Session();
        session.setId(entite.getId());
        session.setTitre(entite.getTitre());
        session.setPromotionId(entite.getPromotionId());
        session.setCode(entite.getCode());
        session.setOuvertureAt(entite.getOuvertureAt());
        session.setExpirationAt(entite.getExpirationAt());
        session.setClotureAt(entite.getClotureAt());
        session.setStatut(entite.getStatut());
        return session;
    }

    public SessionEntity versEntite(Session session) {
        SessionEntity entite = new SessionEntity();
        entite.setId(session.getId());
        entite.setTitre(session.getTitre());
        entite.setPromotionId(session.getPromotionId());
        entite.setCode(session.getCode());
        entite.setOuvertureAt(session.getOuvertureAt());
        entite.setExpirationAt(session.getExpirationAt());
        entite.setClotureAt(session.getClotureAt());
        entite.setStatut(session.getStatut());
        return entite;
    }
}
