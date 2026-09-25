package com.kfokam48.epreuve.presence.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantInconnuException;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.presence.application.dto.AjouterPresenceFormateurRequest;
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
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.dao.DataIntegrityViolationException;
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
    private final EtudiantRepository etudiantRepository;
    private final CompteurDeTentatives compteur;

    public PresenceService(PresenceRepository presenceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository,
                           CompteurDeTentatives compteur) {
        this.presenceRepository = presenceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
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

        Presence enregistree = enregistrerSansDoublon(presence);
        compteur.reinitialiser(etudiantId);

        return new PresenceResponse(enregistree.getId(), enregistree.getSessionId(),
                enregistree.getEtudiantId(), enregistree.getSource());
    }

    /**
     * RG11 (Q14) : le formateur ajoute une présence à la main — « ça arrive qu'un étudiant ait un souci
     * de téléphone ». La présence porte alors {@code source = FORMATEUR}, pour que l'exception se voie
     * dans le tableau au lieu de se fondre dans la masse.
     *
     * <p>Aucun code n'est demandé, et c'est tout l'intérêt : le formateur intervient précisément quand
     * l'étudiant n'a pas pu saisir le sien. Le compteur de RG8 n'est pas touché non plus — ce n'est pas
     * une tentative de l'étudiant.
     *
     * <p>Les verrous restent ceux du reste du module : la session doit exister, ne pas être clôturée
     * (RG13 — un ajout après clôture rouvrirait une session fermée par une porte de service), l'étudiant
     * doit exister, et une présence déjà enregistrée ne se duplique pas (RG7).
     */
    @Transactional
    public PresenceResponse ajouterParFormateur(AjouterPresenceFormateurRequest requete) {
        Session session = sessionRepository.trouverParId(requete.sessionId())
                .orElseThrow(() -> new SessionInconnueException(requete.sessionId()));

        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(session.getId());
        }

        if (etudiantRepository.trouverParId(requete.etudiantId()).isEmpty()) {
            throw new EtudiantInconnuException(requete.etudiantId());
        }

        if (presenceRepository.trouverParSessionEtEtudiant(session.getId(), requete.etudiantId()).isPresent()) {
            throw new DejaPresentException();
        }

        Presence presence = new Presence();
        presence.setSessionId(session.getId());
        presence.setEtudiantId(requete.etudiantId());
        presence.setHorodatage(Instant.now());
        presence.setSource(SourcePresence.FORMATEUR);

        Presence enregistree = enregistrerSansDoublon(presence);
        return new PresenceResponse(enregistree.getId(), enregistree.getSessionId(),
                enregistree.getEtudiantId(), enregistree.getSource());
    }

    /**
     * RG7 / ENF4 — la contrainte d'unicité est portée par la base, et c'est elle qui tranche quand
     * deux saisies arrivent en même temps : la lecture qui précède ne voit alors rien, et c'est
     * l'insertion de la seconde qui échoue.
     *
     * <p>La base a raison de refuser ; ce qui manquait, c'est la traduction. Sans elle, la contrainte
     * remonte en erreur de stockage, donc en {@code 500 ERREUR_INTERNE} — un message que ni l'étudiant
     * ni le formateur ne peuvent comprendre —, là où le contrat impose {@code 409 DEJA_PRESENT}
     * (issue #78). Le contrôle lu avant l'écriture reste utile : dans le cas courant, il évite la
     * tentative d'insertion et donne le même code sans dépendre d'une exception.
     *
     * <p>La même porte sert aux deux cas d'usage, donc les deux sont couverts par cette seule
     * traduction — un ajout manuel du formateur peut lui aussi tomber sur une présence déjà écrite.
     */
    private Presence enregistrerSansDoublon(Presence presence) {
        try {
            return presenceRepository.enregistrer(presence);
        } catch (DataIntegrityViolationException e) {
            throw new DejaPresentException();
        }
    }
}
