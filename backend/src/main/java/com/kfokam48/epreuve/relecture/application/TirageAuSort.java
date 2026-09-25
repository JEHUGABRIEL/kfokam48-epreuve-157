package com.kfokam48.epreuve.relecture.application;

import java.util.List;

/**
 * Tirage au sort du relecteur (RG5, Q7 : « le système, au hasard, parmi les étudiants présents »).
 *
 * <p>Interface plutôt qu'appel direct à {@code Random} : c'est ce qui permet de tester la règle — qui
 * est éligible, qui ne l'est jamais — sans dépendre du hasard. Un tirage non testable aurait laissé
 * RG2 (jamais l'auteur) invérifiable.
 */
public interface TirageAuSort {

    /** @return l'identifiant tiré parmi les candidats ; la liste n'est jamais vide. */
    Long tirer(List<Long> candidats);
}
