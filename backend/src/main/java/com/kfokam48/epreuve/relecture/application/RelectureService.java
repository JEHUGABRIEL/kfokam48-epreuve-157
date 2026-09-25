package com.kfokam48.epreuve.relecture.application;

import com.kfokam48.epreuve.presence.domain.PresenceRepository;
import com.kfokam48.epreuve.presence.domain.model.Presence;
import com.kfokam48.epreuve.relecture.domain.RelectureRepository;
import com.kfokam48.epreuve.relecture.domain.model.Relecture;
import com.kfokam48.epreuve.relecture.domain.model.StatutRelecture;

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
 */
@Service
public class RelectureService {

    private final RelectureRepository relectureRepository;
    private final PresenceRepository presenceRepository;
    private final TirageAuSort tirageAuSort;

    public RelectureService(RelectureRepository relectureRepository,
                            PresenceRepository presenceRepository,
                            TirageAuSort tirageAuSort) {
        this.relectureRepository = relectureRepository;
        this.presenceRepository = presenceRepository;
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
}
