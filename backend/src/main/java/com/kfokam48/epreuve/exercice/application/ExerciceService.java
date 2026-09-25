package com.kfokam48.epreuve.exercice.application;

import com.kfokam48.epreuve.common.referentiel.domain.EtudiantInconnuException;
import com.kfokam48.epreuve.common.referentiel.domain.EtudiantRepository;
import com.kfokam48.epreuve.exercice.application.dto.DeposerExerciceRequest;
import com.kfokam48.epreuve.exercice.application.dto.ExerciceDeposeResponse;
import com.kfokam48.epreuve.exercice.application.dto.RemplacerLienRequest;
import com.kfokam48.epreuve.exercice.domain.ExerciceDejaDeposeException;
import com.kfokam48.epreuve.exercice.domain.ExerciceInconnuException;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;
import com.kfokam48.epreuve.relecture.application.RelectureService;
import com.kfokam48.epreuve.relecture.domain.RelectureDejaRendueException;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Cas d'usage du module exercice : déposer le lien d'un exercice (EF3, RG9, RG13), le remplacer
 * (EF4, RG10) et déclencher le tirage au sort de son relecteur (EF5, RG5).
 *
 * <p>L'ordre des contrôles suit celui du contrat : la session doit exister, ne pas être clôturée,
 * désigner un étudiant réel, et l'étudiant ne doit pas avoir déjà déposé pour cette session.
 *
 * <p>Le tirage au sort appartient au module relecture : il est demandé à son cas d'usage
 * ({@link RelectureService}), jamais à son infrastructure. Un module ne connaît des autres que leur
 * domaine et leurs cas d'usage — la règle de dépendance reste tenue.
 */
@Service
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final RelectureService relectureService;
    private final RelectureRepository relectureRepository;

    public ExerciceService(ExerciceRepository exerciceRepository,
                           SessionRepository sessionRepository,
                           EtudiantRepository etudiantRepository,
                           RelectureService relectureService,
                           RelectureRepository relectureRepository) {
        this.exerciceRepository = exerciceRepository;
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.relectureService = relectureService;
        this.relectureRepository = relectureRepository;
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

        // EF5 / RG5 : le tirage a lieu dans la transaction du dépôt — un exercice enregistré sans son
        // relecteur serait un exercice que personne ne corrige (RG4). Si aucun présent n'est éligible,
        // l'exercice reste DEPOSE et le formateur le voit dans son tableau (Q11, §7).
        boolean relecteurAssigne = relectureService.assigner(
                enregistre.getId(), enregistre.getSessionId(), enregistre.getEtudiantId()).isPresent();
        if (relecteurAssigne) {
            enregistre.marquerEnAttenteDeRelecture();
            enregistre = exerciceRepository.enregistrer(enregistre);
        }

        return new ExerciceDeposeResponse(enregistre.getId(), enregistre.getStatut());
    }

    /**
     * EF4 / RG10 (Q13) : remplacer le lien d'un exercice déjà déposé.
     *
     * <p>La condition retenue est « tant que la relecture n'est pas <em>rendue</em> », et non la lettre
     * de Q13 (« personne n'a commencé à le relire ») : aucun état ne représente une relecture
     * commencée, la fenêtre est donc plus large que ce que le client a dit. L'écart est assumé et
     * documenté en §7 plutôt que découvert à la correction.
     */
    @Transactional
    public ExerciceDeposeResponse remplacerLien(Long exerciceId, RemplacerLienRequest requete) {
        Exercice.verifierLien(requete.lien());

        Exercice exercice = exerciceRepository.trouverParId(exerciceId)
                .orElseThrow(() -> new ExerciceInconnuException(exerciceId));

        Session session = sessionRepository.trouverParId(exercice.getSessionId())
                .orElseThrow(() -> new SessionInconnueException(exercice.getSessionId()));
        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(session.getId());
        }

        // RG10 : une fois la relecture rendue, le travail relu ne change plus sous les pieds du relecteur.
        Optional<Relecture> relecture = relectureRepository.trouverParExerciceId(exerciceId);
        if (relecture.isPresent() && relecture.get().estRendue()) {
            throw new RelectureDejaRendueException();
        }

        exercice.setLien(requete.lien().trim());
        Exercice enregistre = exerciceRepository.enregistrer(exercice);
        return new ExerciceDeposeResponse(enregistre.getId(), enregistre.getStatut());
    }
}
