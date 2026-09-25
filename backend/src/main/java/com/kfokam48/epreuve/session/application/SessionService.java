package com.kfokam48.epreuve.session.application;

import com.kfokam48.epreuve.session.application.dto.OuvrirSessionRequest;
import com.kfokam48.epreuve.session.application.dto.SessionOuverteResponse;
import com.kfokam48.epreuve.session.domain.Session;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.StatutSession;

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

        Session enregistree = sessionRepository.save(session);

        return new SessionOuverteResponse(
                enregistree.getId(),
                enregistree.getCode(),
                enregistree.getOuvertureAt(),
                enregistree.getExpirationAt()
        );
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
