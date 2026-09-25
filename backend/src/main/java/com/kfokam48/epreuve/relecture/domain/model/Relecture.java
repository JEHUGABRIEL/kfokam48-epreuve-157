package com.kfokam48.epreuve.relecture.domain.model;

import com.kfokam48.epreuve.relecture.domain.NoteInvalideException;
import com.kfokam48.epreuve.relecture.domain.RelectureDejaRendueException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Relecture attribuée à un étudiant pour un exercice donné (cf. D2).
 * <ul>
 *   <li>RG4, <strong>révisée à l'étape 3</strong> : un exercice est relu par deux pairs distincts, et
 *       la note retenue est la moyenne des deux — cf. {@link NoteRetenue}.</li>
 *   <li>RG6 : l'identité du relecteur ne sort jamais de l'API.</li>
 * </ul>
 *
 * <p>Il n'existe pas d'entité {@code Relecteur} : le relecteur est un {@code Etudiant} désigné par
 * {@code relecteurId}, tiré au sort à l'instant du dépôt (RG5).
 *
 * <p>Aucune annotation de persistance ici : la table est décrite par {@code RelectureEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Relecture {

    /**
     * RG4 révisée : le nombre de pairs attendus sur un exercice. Une constante, et une seule, parce
     * que cette valeur décide à la fois du nombre de relectures assignées et du moment où la note
     * retenue cesse d'être provisoire : deux endroits, mais une seule vérité.
     */
    public static final int PAIRS_ATTENDUS = 2;

    private Long id;
    private Long exerciceId;
    private Long relecteurId;
    private Integer note;
    private String commentaire;
    private StatutRelecture statut = StatutRelecture.ASSIGNEE;
    private Instant assigneeAt;
    private Instant renduAt;

    /** RG12 : une relecture rendue est définitive — c'est cet état qui verrouille le lien (RG10). */
    public boolean estRendue() {
        return statut == StatutRelecture.RENDUE;
    }

    /**
     * Rend la note et le commentaire (EF6). Une seule fois, pour toujours.
     *
     * <p>Les deux règles du client sont ici, et pas seulement dans le service : RG3 (note entière de 0
     * à 20, Q9) et RG12 (Q15 : « une fois que le relecteur a validé, c'est fini »). Les placer dans le
     * modèle, c'est pouvoir les tester sans base ni contexte Spring.
     *
     * <p>Le verrou est vérifié avant la note : une relecture déjà rendue le reste, quelle que soit la
     * note qu'on essaie d'y écrire.
     */
    public void rendre(Integer note, String commentaire) {
        if (estRendue()) {
            throw new RelectureDejaRendueException();
        }
        if (note == null || note < 0 || note > 20) {
            throw new NoteInvalideException();
        }
        this.note = note;
        this.commentaire = commentaire;
        this.statut = StatutRelecture.RENDUE;
        this.renduAt = Instant.now();
    }
}
