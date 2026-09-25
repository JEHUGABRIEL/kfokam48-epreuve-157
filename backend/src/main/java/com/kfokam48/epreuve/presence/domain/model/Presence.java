package com.kfokam48.epreuve.presence.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Présence d'un étudiant à une session (cf. D2), sans aucune dépendance de persistance.
 *
 * <ul>
 *   <li>RG7 : une seule présence par étudiant et par session.</li>
 *   <li>ENF4 : l'unicité {@code (sessionId, etudiantId)} est aussi garantie par la base.</li>
 *   <li>RG11 : une présence ajoutée par le formateur porte {@code source = FORMATEUR}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class Presence {

    private Long id;
    private Long sessionId;
    private Long etudiantId;
    private Instant horodatage;
    private SourcePresence source = SourcePresence.ETUDIANT;
}
