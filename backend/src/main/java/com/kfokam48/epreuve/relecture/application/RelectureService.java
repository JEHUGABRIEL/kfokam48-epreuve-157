package com.kfokam48.epreuve.relecture.application;

import com.kfokam48.epreuve.common.pagination.application.ResultatPage;
import com.kfokam48.epreuve.common.pagination.domain.PageDemandee;
import com.kfokam48.epreuve.exercice.domain.ExerciceInconnuException;
import com.kfokam48.epreuve.exercice.domain.ExerciceRepository;
import com.kfokam48.epreuve.exercice.domain.model.Exercice;
import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.relecture.application.dto.NoteRecueResponse;
import com.kfokam48.epreuve.relecture.application.dto.RelectureAssigneeResponse;
import com.kfokam48.epreuve.relecture.application.dto.RelectureResponse;
import com.kfokam48.epreuve.relecture.application.dto.RendreRelectureRequest;
import com.kfokam48.epreuve.relecture.domain.AutoRelectureException;
import com.kfokam48.epreuve.relecture.domain.RelectureInconnueException;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.NoteRetenue;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;
import com.kfokam48.epreuve.session.domain.SessionDejaClotureeException;
import com.kfokam48.epreuve.session.domain.SessionInconnueException;
import com.kfokam48.epreuve.session.domain.SessionRepository;
import com.kfokam48.epreuve.session.domain.model.Session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cas d'usage du module relecture.
 *
 * <p>Assigner deux relecteurs au dépôt d'un exercice : EF5, RG2, RG4 (révisée à l'étape 3), RG5. Ce
 * module est le seul à écrire une {@code Relecture}, donc c'est ici que la règle RG5 existe — et c'est
 * donc ici qu'elle se teste.
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
     * Tire au sort **deux** relecteurs parmi les étudiants <strong>déjà présents</strong> à la session,
     * jamais l'auteur de l'exercice, et distincts entre eux (RG2, RG4 révisée, RG5, Q7).
     *
     * <p>Le second est tiré parmi les candidats qui restent, après retrait du premier : deux tirages
     * successifs sur la même liste pouvaient désigner deux fois la même personne, et il aurait fallu
     * s'en apercevoir après coup pour recommencer — un tirage qu'on doit rattraper n'est pas un tirage.
     *
     * <p>Le tirage se fait à l'instant du dépôt : une présence ajoutée après coup par le formateur
     * (Q14) n'entre pas dans un pool déjà tiré — limite connue, écrite en §7.
     *
     * <p>S'il n'y a qu'un candidat, un seul relecteur est assigné : l'exercice est relu une fois, et sa
     * note restera provisoire puisqu'aucun second pair n'existe (§7). L'alternative — ne rien assigner —
     * aurait laissé l'exercice hors circuit alors qu'une note est disponible.
     *
     * @return la première relecture assignée, ou vide si aucun présent n'est éligible (classe réduite,
     *     ou seul l'auteur est présent) : l'exercice reste alors {@code DEPOSE}, visible dans le tableau
     *     comme une relecture non rendue (Q11, §7).
     */
    @Transactional
    public Optional<Relecture> assigner(Long exerciceId, Long sessionId, Long auteurId) {
        List<Long> eligibles = new ArrayList<>(presenceRepository.trouverParSession(sessionId).stream()
                .map(Presence::getEtudiantId)
                .distinct()
                .filter(etudiantId -> !etudiantId.equals(auteurId))
                .toList());

        List<Long> relecteurs = new ArrayList<>();
        while (relecteurs.size() < Relecture.PAIRS_ATTENDUS && !eligibles.isEmpty()) {
            // Le tirage reçoit une copie : la liste que ce module possède continue d'évoluer, mais ce
            // que le second tirage voit ne réécrit pas ce qu'a vu le premier. Passer la même liste
            // mutable aux deux tirages les rendait indiscernables dès qu'on voulait les relire.
            Long relecteurId = tirageAuSort.tirer(List.copyOf(eligibles));
            eligibles.remove(relecteurId);
            relecteurs.add(relecteurId);
        }

        Relecture premiere = null;
        for (Long relecteurId : relecteurs) {
            Relecture relecture = new Relecture();
            relecture.setExerciceId(exerciceId);
            relecture.setRelecteurId(relecteurId);
            relecture.setStatut(StatutRelecture.ASSIGNEE);
            relecture.setAssigneeAt(Instant.now());

            Relecture enregistree = relectureRepository.enregistrer(relecture);
            if (premiere == null) {
                premiere = enregistree;
            }
        }

        return Optional.ofNullable(premiere);
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

        // D4, révisé à l'étape 3 : l'exercice n'est RELU que lorsque **toutes** ses relectures
        // assignées sont rendues. Tant qu'une note manque, la note retenue n'existe pas ou n'est que
        // provisoire, et l'exercice n'a pas fini son parcours.
        //
        // Le remplacement du lien (RG10), lui, s'est déjà fermé à la **première** note rendue : la
        // fenêtre du lien et l'état de l'exercice répondent à deux questions différentes (§7).
        boolean toutesRendues = relectureRepository.trouverParExerciceId(exercice.getId()).stream()
                .allMatch(Relecture::estRendue);
        if (toutesRendues) {
            exercice.marquerRelu();
            exerciceRepository.enregistrer(exercice);
        }

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
    public ResultatPage<RelectureAssigneeResponse> listerAssignees(Long relecteurId,
                                                                   Optional<PageDemandee> pagination) {
        List<Relecture> assignees;
        long total;
        if (pagination.isEmpty()) {
            assignees = relectureRepository.listerParRelecteurEtStatut(relecteurId, StatutRelecture.ASSIGNEE);
            total = assignees.size();
        } else {
            assignees = relectureRepository.listerParRelecteurEtStatut(relecteurId, StatutRelecture.ASSIGNEE,
                    pagination.get());
            total = relectureRepository.compterParRelecteurEtStatut(relecteurId, StatutRelecture.ASSIGNEE);
        }

        List<RelectureAssigneeResponse> reponses = assignees.stream()
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
        return new ResultatPage<>(reponses, total);
    }

    /**
     * EF7 / RG6 (Q8) : l'étudiant relu consulte sa note et le commentaire reçu, jamais l'identité de
     * ceux qui l'ont relu.
     *
     * <p>Le contrat imposé n'offrait aucun moyen de <em>lire</em> une note, seulement d'en écrire une :
     * EF7 aurait été invérifiable sans cette opération. Tant qu'aucune relecture n'est rendue, le statut
     * vaut {@code ASSIGNEE} et la note est nulle — l'étudiant sait qu'il attend, sans savoir qui.
     *
     * <p>Depuis l'étape 3, ce que l'étudiant lit est la <strong>note retenue</strong> de l'exercice
     * (cf. {@link NoteRetenue}) et non plus celle d'un relecteur unique, avec son caractère provisoire :
     * « 13,5 — en attente du second relecteur » est une information honnête, alors qu'une moyenne
     * présentée comme définitive ne le serait pas.
     */
    @Transactional(readOnly = true)
    public NoteRecueResponse consulterRecue(Long etudiantId, Long sessionId) {
        Exercice exercice = exerciceRepository.trouverParSessionEtEtudiant(sessionId, etudiantId)
                .orElseThrow(RelectureInconnueException::new);

        List<Relecture> relectures = relectureRepository.trouverParExerciceId(exercice.getId());
        if (relectures.isEmpty()) {
            throw new RelectureInconnueException();
        }

        Optional<NoteRetenue> retenue = NoteRetenue.depuis(relectures);
        if (retenue.isEmpty()) {
            // Déposé, assigné, pas encore relu : il n'y a rien à afficher, et ce n'est pas une erreur.
            return new NoteRecueResponse(StatutRelecture.ASSIGNEE, null, null, false);
        }

        return new NoteRecueResponse(StatutRelecture.RENDUE, retenue.get().note(),
                commentairesRendus(relectures), retenue.get().provisoire());
    }

    /**
     * Les commentaires des relectures rendues, mis bout à bout, du premier rendu au dernier (§7).
     *
     * <p>La forme de la réponse n'a qu'un champ {@code commentaire}, hérité de l'ajout fait avant le
     * changement de besoin : on ne la casse pas, on la remplit. Les noms n'y figurent pas — RG6 n'a pas
     * été assoupli par le passage à deux relecteurs —, et l'ordre de rendu est le seul repère donné au
     * lecteur, ce qui évite de laisser croire qu'un seul pair s'est exprimé.
     */
    private String commentairesRendus(List<Relecture> relectures) {
        String concatene = relectures.stream()
                .filter(Relecture::estRendue)
                .sorted(Comparator.comparing(Relecture::getRenduAt).thenComparing(Relecture::getId))
                .map(Relecture::getCommentaire)
                .filter(commentaire -> commentaire != null && !commentaire.isBlank())
                .collect(Collectors.joining(" — "));

        return concatene.isEmpty() ? null : concatene;
    }
}
