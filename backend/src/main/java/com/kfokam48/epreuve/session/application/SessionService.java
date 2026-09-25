package com.kfokam48.epreuve.session.application;

import com.kfokam48.epreuve.session.application.dto.OuvrirSessionRequest;
import com.kfokam48.epreuve.session.application.dto.SessionClotureeResponse;
import com.kfokam48.epreuve.session.application.dto.SessionOuverteResponse;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;
import com.kfokam48.epreuve.session.domain.model.StatutSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class SessionService {

    // RG1 : le code de présence expire 15 minutes après l'ouverture de la session.
    private static final Duration DUREE_VALIDITE_CODE = Duration.ofMinutes(15);

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public SessionOuverteResponse ouvrir(OuvrirSessionRequest requete) {
        Session session = new Session();
        session.setTitre(requete.titre());
        session.setPromotionId(requete.promotionId());
        session.setCode(genererCode());

        Instant maintenant = Instant.now();
        session.setOuvertureAt(maintenant);
        session.setExpirationAt(maintenant.plus(DUREE_VALIDITE_CODE));
        session.setStatut(StatutSession.OUVERTE);

        Session enregistree = sessionRepository.enregistrer(session);

        return new SessionOuverteResponse(
                enregistree.getId(),
                enregistree.getCode(),
                enregistree.getOuvertureAt(),
                enregistree.getExpirationAt()
        );
    }

    // RG13 : la clôture verrouille la session. C'est ici qu'on l'écrit, et c'est aux autres
    // modules de la consulter avant toute écriture — un seul endroit décide de l'état.
    @Transactional
    public SessionClotureeResponse cloturer(Long id) {
        Session session = sessionRepository.trouverParId(id)
                .orElseThrow(() -> new SessionInconnueException(id));

        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(id);
        }

        session.setStatut(StatutSession.CLOTUREE);
        session.setClotureAt(Instant.now());
        sessionRepository.enregistrer(session);

        return new SessionClotureeResponse(session.getId(), session.getClotureAt());
    }

    // Alphabet volontairement sans caractères ambigus (0/O, 1/I) : le code est destiné
    // à être lu ou écrit au tableau par le formateur (contexte présentiel, cf. §7).
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 6;
    private final SecureRandom random = new SecureRandom();

    private String genererCode() {
        StringBuilder code = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
