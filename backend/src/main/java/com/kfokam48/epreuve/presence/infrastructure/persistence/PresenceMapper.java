package com.kfokam48.epreuve.presence.infrastructure.persistence;

import com.kfokam48.epreuve.presence.domain.model.Presence;

import org.springframework.stereotype.Component;

/** Traduit entre le modèle du domaine et l'entité de stockage, champ par champ. */
@Component
public class PresenceMapper {

    public Presence versModele(PresenceEntity entite) {
        Presence presence = new Presence();
        presence.setId(entite.getId());
        presence.setSessionId(entite.getSessionId());
        presence.setEtudiantId(entite.getEtudiantId());
        presence.setHorodatage(entite.getHorodatage());
        presence.setSource(entite.getSource());
        return presence;
    }

    public PresenceEntity versEntite(Presence presence) {
        PresenceEntity entite = new PresenceEntity();
        entite.setId(presence.getId());
        entite.setSessionId(presence.getSessionId());
        entite.setEtudiantId(presence.getEtudiantId());
        entite.setHorodatage(presence.getHorodatage());
        entite.setSource(presence.getSource());
        return entite;
    }
}
