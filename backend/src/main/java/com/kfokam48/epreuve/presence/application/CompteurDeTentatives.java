package com.kfokam48.epreuve.presence.application;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RG8 : cinq échecs de code par étudiant, puis deux minutes de blocage.
 *
 * <p>Le compteur vit <strong>en mémoire applicative, indexé par {@code etudiantId} seul</strong>
 * (cf. cahier des charges §7). Deux raisons à cette clé, et non {@code (etudiantId, sessionId)} :
 * ENF5 exclut toute session utilisateur, et un code inconnu — 400 {@code CODE_INCONNU} — ne permet
 * pas de retrouver la session visée, donc la clé composée serait inutilisable dans le cas le plus
 * fréquent.
 *
 * <p>Conséquence assumée : l'état est volatil. Un redémarrage remet les compteurs à zéro, ce qui est
 * acceptable pour un blocage de deux minutes et évite une table dont le cycle de vie n'est pas
 * demandé.
 */
@Component
public class CompteurDeTentatives {

    private static final int SEUIL_ECHECS = 5;
    private static final Duration DUREE_BLOCAGE = Duration.ofMinutes(2);

    /** Échecs consécutifs depuis le dernier succès, et fin du blocage s'il est en cours. */
    private record Etat(int echecs, Instant bloqueJusqua) {
    }

    private final Map<Long, Etat> etatsParEtudiant = new ConcurrentHashMap<>();

    public boolean estBloque(Long etudiantId) {
        Etat etat = etatsParEtudiant.get(etudiantId);
        if (etat == null || etat.bloqueJusqua() == null) {
            return false;
        }
        if (Instant.now().isBefore(etat.bloqueJusqua())) {
            return true;
        }
        // Le blocage est échu : on repart d'un compteur vierge plutôt que de le laisser traîner.
        etatsParEtudiant.remove(etudiantId);
        return false;
    }

    /** Enregistre un échec et déclenche le blocage au cinquième, pas avant. */
    public void enregistrerEchec(Long etudiantId) {
        etatsParEtudiant.compute(etudiantId, (id, etat) -> {
            int echecs = (etat == null ? 0 : etat.echecs()) + 1;
            if (echecs >= SEUIL_ECHECS) {
                return new Etat(0, Instant.now().plus(DUREE_BLOCAGE));
            }
            return new Etat(echecs, null);
        });
    }

    /** Un code qui fonctionne remet le compteur à zéro : l'étudiant n'est plus en faute. */
    public void reinitialiser(Long etudiantId) {
        etatsParEtudiant.remove(etudiantId);
    }
}
