package com.kfokam48.epreuve.relecture.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * La note retenue d'un exercice, et son caractère provisoire (RG4 révisée à l'étape 3).
 *
 * <ul>
 *   <li>Deux relectures rendues → la moyenne des deux : la note est <strong>définitive</strong>.</li>
 *   <li>Une seule rendue → cette note, mais <strong>provisoire</strong> : le second pair peut encore
 *       rendre la sienne, et la note retenue changera alors.</li>
 *   <li>Aucune rendue → aucune note. « Pas encore de note » n'est pas « zéro », et ce n'est pas non
 *       plus une erreur : c'est le cas normal d'un exercice déposé du matin.</li>
 * </ul>
 *
 * <p>Le calcul vit ici, dans le domaine, et pas chez ses appelants. C'est une règle de gestion, donc
 * elle se teste sans base ni contexte Spring ; et c'est la seule façon d'éviter que le tableau, la
 * consultation par l'étudiant et le rendu d'une relecture n'en donnent chacun une version légèrement
 * différente — trois moyennes qui divergent, c'est trois vérités.
 */
public record NoteRetenue(double note, boolean provisoire) {

    /**
     * Deux décimales (cf. §7). La moyenne de deux notes entières ne produit que des demis, mais la
     * règle est écrite telle que le client l'a demandée — la moyenne des deux notes, pas un
     * arrangement — et restera vraie si le nombre de relecteurs change encore.
     */
    private static final int DECIMALES = 2;

    /**
     * @return la note retenue, ou vide si aucune relecture n'est rendue. Le vide n'est pas une erreur :
     *     c'est un exercice qui attend encore.
     */
    public static Optional<NoteRetenue> depuis(List<Relecture> relectures) {
        List<Relecture> rendues = relectures.stream().filter(Relecture::estRendue).toList();
        if (rendues.isEmpty()) {
            return Optional.empty();
        }

        double moyenne = rendues.stream().mapToInt(Relecture::getNote).average().orElseThrow();

        // Provisoire tant qu'un pair attendu n'a pas rendu (cf. §7) : avec un seul étudiant éligible,
        // la note reste provisoire pour toujours — c'est dit, plutôt que de la faire passer pour
        // définitive.
        boolean provisoire = rendues.size() < Relecture.PAIRS_ATTENDUS;

        return Optional.of(new NoteRetenue(arrondir(moyenne), provisoire));
    }

    private static double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(DECIMALES, RoundingMode.HALF_UP).doubleValue();
    }
}
