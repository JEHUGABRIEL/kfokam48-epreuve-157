package com.kfokam48.epreuve.relecture.application;

import com.kfokam48.epreuve.exercice.domain.ExerciceInconnuException;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.relecture.application.dto.RelectureAssigneeResponse;
import com.kfokam48.epreuve.relecture.application.dto.RelectureResponse;
import com.kfokam48.epreuve.relecture.application.dto.RendreRelectureRequest;
import com.kfokam48.epreuve.relecture.domain.AutoRelectureException;
import com.kfokam48.epreuve.relecture.domain.RelectureInconnueException;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Cas d'usage du module relecture.
 *
 * <p>Assigner un relecteur au dépôt d'un exercice : EF5, RG2, RG4, RG5. Ce module est le seul à écrire
 * une {@code Relecture}, donc c'est ici que la règle RG5 existe — et c'est donc ici qu'elle se teste.
 *
 * <p>Appelé par {@code ExerciceService} dans la même transaction que le dépôt : un exercice enregistré
 * sans son tirage serait un exercice que personne ne corrige (RG4).
 *
 * <p>Puis rendre une note et un commentaire sur la relecture assignée : EF6, RG2, RG3, RG12, RG13, et
 * exposer à l'étudiant relu sa note et son commentaire sans l'identité du relecteur : EF7, RG6.
 * Ici, le module ne dépend que des <em>ports</em> des autres modules (session, exercice) — jamais de
 * leur infrastructure, ce qui tient la règle de dépendance.
 */
@Service
public class RelectureService {

    private final RelectureRepository relectureRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final TirageAuSort tirageAuSort;

    public RelectureService(RelectureRepository relectureRepository,
                            PresenceRepository presenceRepository,
                            ExerciceRepository exerciceRepository,
                            SessionRepository sessionRepository,
                            TirageAuSort tirageAuSort) {
        this.relectureRepository = relectureRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.sessionRepository = sessionRepository;
        this.tirageAuSort = tirageAuSort;
    }

    /**
     * Tire au sort un relecteur parmi les étudiants <strong>déjà présents</strong> à la session, et
     * jamais l'auteur de l'exercice (RG2, RG5, Q7).
     *
     * <p>Le tirage se fait à l'instant du dépôt : une présence ajoutée après coup par le formateur
     * (Q14) n'entre pas dans un pool déjà tiré — limite connue, écrite en §7.
     *
     * @return la relecture assignée, ou vide si aucun présent n'est éligible (classe réduite, ou seul
     *     l'auteur est présent) : l'exercice reste alors {@code DEPOSE}, visible dans le tableau comme
     *     une relecture non rendue (Q11, §7).
     */
    @Transactional
    public Optional<Relecture> assigner(Long exerciceId, Long sessionId, Long auteurId) {
        List<Long> candidats = presenceRepository.trouverParSession(sessionId).stream()
                .map(Presence::getEtudiantId)
                .distinct()
                .filter(etudiantId -> !etudiantId.equals(auteurId))
                .toList();

        if (candidats.isEmpty()) {
            return Optional.empty();
        }

        Relecture relecture = new Relecture();
        relecture.setExerciceId(exerciceId);
        relecture.setRelecteurId(tirageAuSort.tirer(candidats));
        relecture.setStatut(StatutRelecture.ASSIGNEE);
        relecture.setAssigneeAt(Instant.now());

        return Optional.of(relectureRepository.enregistrer(relecture));
    }

    /**
     * Le relecteur rend sa note et son commentaire : EF6, RG2, RG3, RG12, RG13.
     *
     * <p>L'ordre des contrôles est celui des règles : la relecture existe, l'auteur n'est pas le
     * relecteur (403 imposé par le contrat), la session n'est pas clôturée (RG13), puis le domaine
     * applique le verrou RG12 et la borne RG3.
     */
    @Transactional
    public RelectureResponse rendre(Long relectureId, RendreRelectureRequest requete) {
        Relecture relecture = relectureRepository.trouverParId(relectureId)
                .orElseThrow(() -> new RelectureInconnueException(relectureId));

        Exercice exercice = exerciceRepository.trouverParId(relecture.getExerciceId())
                .orElseThrow(() -> new ExerciceInconnuException(relecture.getExerciceId()));

        // RG2 : le tirage ne désigne jamais l'auteur (RG5), mais l'écriture le revérifie — c'est cette
        // opération que le contrat impose en 403 AUTO_RELECTURE. Une règle qu'on ne peut pas
        // revérifier au moment d'écrire n'est pas une règle, c'est une espérance.
        if (relecture.getRelecteurId().equals(exercice.getEtudiantId())) {
            throw new AutoRelectureException();
        }

        // RG13 : la clôture verrouille les relectures comme le reste (Q10, Q11, Q15).
        Session session = sessionRepository.trouverParId(exercice.getSessionId())
                .orElseThrow(() -> new SessionInconnueException(exercice.getSessionId()));
        if (session.estCloturee()) {
            throw new SessionDejaClotureeException(session.getId());
        }

        relecture.rendre(requete.note(), requete.commentaire());
        Relecture enregistree = relectureRepository.enregistrer(relecture);

        // D4 : l'exercice passe RELU, ce qui ferme la fenêtre de remplacement du lien (RG10).
        exercice.marquerRelu();
        exerciceRepository.enregistrer(exercice);

        return new RelectureResponse(enregistree.getStatut(), enregistree.getNote(),
                enregistree.getCommentaire());
    }

    /**
     * Ce qu'un relecteur doit encore rendre (UC7).
     *
     * <p>Cette lecture n'est pas un confort : le relecteur ne connaît l'identifiant de sa relecture que
     * par elle. Sans elle, {@code POST /api/relectures/{id}} resterait inatteignable — l'opération
     * imposée existerait sans que personne puisse l'appeler.
     */
    @Transactional(readOnly = true)
    public List<RelectureAssigneeResponse> listerAssignees(Long relecteurId) {
        return relectureRepository.listerParRelecteurEtStatut(relecteurId, StatutRelecture.ASSIGNEE).stream()
                .map(relecture -> {
                    Exercice exercice = exerciceRepository.trouverParId(relecture.getExerciceId())
                            .orElseThrow(() -> new ExerciceInconnuException(relecture.getExerciceId()));
                    return new RelectureAssigneeResponse(
                            relecture.getId(),
                            relecture.getExerciceId(),
                            exercice.getSessionId(),
                            exercice.getLien(),
                            relecture.getStatut());
                })
                .toList();
    }

    /**
     * EF7 / RG6 (Q8) : l'étudiant relu consulte sa note et le commentaire reçu, jamais l'identité de
     * celui qui l'a relu.
     *
     * <p>Le contrat imposé n'offrait aucun moyen de <em>lire</em> une note, seulement d'en écrire une :
     * EF7 aurait été invérifiable sans cette opération. Tant que le relecteur n'a pas rendu, le statut
     * vaut {@code ASSIGNEE} et la note est nulle — l'étudiant sait qu'il attend, sans savoir qui.
     */
    @Transactional(readOnly = true)
    public RelectureResponse consulterRecue(Long etudiantId, Long sessionId) {
        Exercice exercice = exerciceRepository.trouverParSessionEtEtudiant(sessionId, etudiantId)
                .orElseThrow(RelectureInconnueException::new);

        return relectureRepository.trouverParExerciceId(exercice.getId())
                .map(relecture -> new RelectureResponse(relecture.getStatut(), relecture.getNote(),
                        relecture.getCommentaire()))
                .orElseThrow(RelectureInconnueException::new);
    }
}
