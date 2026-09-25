package com.kfokam48.epreuve.relecture.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

/**
 * Tirage uniforme parmi les candidats. {@code SecureRandom} plutôt que {@code Random} : un tirage
 * devinable permettrait d'organiser sa propre relecture, et Q4 (« sinon ils vont deviner les codes
 * entre eux ») montre que le client se soucie de ce genre de contournement.
 */
@Component
public class TirageAuSortAleatoire implements TirageAuSort {

    private final SecureRandom random = new SecureRandom();

    @Override
    public Long tirer(List<Long> candidats) {
        return candidats.get(random.nextInt(candidats.size()));
    }
}
