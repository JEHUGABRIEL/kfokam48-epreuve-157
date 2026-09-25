package com.kfokam48.epreuve.presence.application;

import com.kfokam48.epreuve.presence.application.dto.MarquerPresenceRequest;
import com.kfokam48.epreuve.presence.application.dto.PresenceResponse;
import com.kfokam48.epreuve.presence.domain.CodeExpireException;
import com.kfokam48.epreuve.presence.domain.CodeInconnuException;
import com.kfokam48.epreuve.presence.domain.DejaPresentException;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.TropDeTentativesException;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.presence.domain.model.SourcePresence;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Cas d'usage du module presence.
 *
 * <p>L'ordre des contrôles est le fond du sujet, pas un détail : blocage (RG8), puis code inconnu
 * (400), puis session clôturée (RG13), puis code expiré (410), puis présence déjà enregistrée (409).
 */
@Service
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final SessionRepository sessionRepository;
    private final CompteurDeTentatives compteur;

    public PresenceService(PresenceRepository presenceRepository,
                           SessionRepository sessionRepository,
                           CompteurDeTentatives compteur) {
        this.presenceRepository = presenceRepository;
        this.sessionRepository = sessionRepository;
        this.compteur = compteur;
    }

    // EF1, RG1, RG7, RG8, RG13
    @Transactional
    public PresenceResponse marquer(MarquerPresenceRequest requete) {
        Long etudiantId = requete.etudiantId();

        // RG8 d'abord, et avant toute lecture du code : un étudiant bloqué reçoit le même refus quel
        // que soit ce qu'il saisit, y compris un code inconnu (cf. §7). Sinon, le blocage deviendrait
        // un moyen de tester des codes un par un.
        if (compteur.estBloque(etudiantId)) {
            throw new TropDeTentativesException();
        }

        Optional<Session> sessionTrouvee = sessionRepository.trouverParCode(requete.code());
        if (sessionTrouvee.isEmpty()) {
            // Un code inexistant est bien une erreur de saisie : il compte pour RG8.
            compteur.enregistrerEchec(etudiantId);
            throw new CodeInconnuException();
        }
        Session session = sessionTrouvee.get();

        // RG13 : session clôturée. Ce n'est pas une erreur de code, donc le compteur n'est pas touché.
        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(session.getId());
        }

        // RG1 : la validité se lit sur la session (ouverture + 15 minutes), jamais sur un minuteur client.
        if (session.estExpiree()) {
            compteur.enregistrerEchec(etudiantId);
            throw new CodeExpireException();
        }

        // RG7 : une seule présence par étudiant et par session. La base porte aussi la contrainte
        // (ENF4), celle-ci n'évite que le cas courant et donne un message clair.
        if (presenceRepository.trouverParSessionEtEtudiant(session.getId(), etudiantId).isPresent()) {
            throw new DejaPresentException();
        }

        Presence presence = new Presence();
        presence.setSessionId(session.getId());
        presence.setEtudiantId(etudiantId);
        presence.setHorodatage(Instant.now());
        presence.setSource(SourcePresence.ETUDIANT);

        Presence enregistree = presenceRepository.enregistrer(presence);
        compteur.reinitialiser(etudiantId);

        return new PresenceResponse(enregistree.getId(), enregistree.getSessionId(),
                enregistree.getEtudiantId(), enregistree.getSource());
    }
}
