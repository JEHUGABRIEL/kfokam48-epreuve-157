package com.kfokam48.epreuve.exercice.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantInconnuException;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.exercice.application.dto.DeposerExerciceRequest;
import com.kfokam48.epreuve.exercice.application.dto.ExerciceDeposeResponse;
import com.kfokam48.epreuve.exercice.domain.ExerciceDejaDeposeException;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Cas d'usage du module exercice : déposer le lien d'un exercice (EF3, RG9, RG13).
 *
 * <p>L'ordre des contrôles suit celui du contrat : la session doit exister, ne pas être clôturée,
 * désigner un étudiant réel, et l'étudiant ne doit pas avoir déjà déposé pour cette session.
 */
@Service
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;

    public ExerciceService(ExerciceRepository exerciceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository) {
        this.exerciceRepository = exerciceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    // EF3, RG9, RG13 — §7 : la présence n'est pas une condition du dépôt, ni l'heure de fin théorique.
    @Transactional
    public ExerciceDeposeResponse deposer(DeposerExerciceRequest requete) {
        Exercice.verifierLien(requete.lien());

        Session session = sessionRepository.trouverParId(requete.sessionId())
                .orElseThrow(() -> new SessionInconnueException(requete.sessionId()));

        // RG13 : plus aucun dépôt après clôture par le formateur (Q3, Q12).
        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(session.getId());
        }

        if (etudiantRepository.trouverParId(requete.etudiantId()).isEmpty()) {
            throw new EtudiantInconnuException(requete.etudiantId());
        }

        // Un étudiant dépose un seul exercice par session (le schéma porte aussi l'unicité).
        if (exerciceRepository.trouverParSessionEtEtudiant(requete.sessionId(), requete.etudiantId()).isPresent()) {
            throw new ExerciceDejaDeposeException();
        }

        Exercice exercice = new Exercice();
        exercice.setSessionId(session.getId());
        exercice.setEtudiantId(requete.etudiantId());
        exercice.setLien(requete.lien().trim());
        exercice.setDeposeAt(Instant.now());

        Exercice enregistre = exerciceRepository.enregistrer(exercice);
        return new ExerciceDeposeResponse(enregistre.getId(), enregistre.getStatut());
    }
}
