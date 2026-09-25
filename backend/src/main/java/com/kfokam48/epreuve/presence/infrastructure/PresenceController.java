package com.kfokam48.epreuve.presence.infrastructure;

import com.kfokam48.epreuve.presence.application.PresenceService;
import com.kfokam48.epreuve.presence.application.dto.AjouterPresenceFormateurRequest;
import com.kfokam48.epreuve.presence.application.dto.MarquerPresenceRequest;
import com.kfokam48.epreuve.presence.application.dto.PresenceResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapte le module presence au contrat {@code api/contrat.yaml}.
 *
 * <p>{@code POST /api/presences} → 201 ; 400 {@code CODE_INCONNU} ; 409 {@code DEJA_PRESENT} ;
 * 410 {@code CODE_EXPIRE} ; et, hors contrat imposé, 429 {@code TROP_DE_TENTATIVES} (RG8, cf. §7).
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    // EF1 — l'étudiant marque sa présence avec le code annoncé en salle.
    @PostMapping
    public ResponseEntity<PresenceResponse> marquer(@Valid @RequestBody MarquerPresenceRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presenceService.marquer(requete));
    }

    // RG11 / Q14 — le formateur ajoute une présence à la main (souci de téléphone).
    // Ajouté hors contrat imposé après le gel du contrat, parce que sans cette opération RG11 n'était
    // pas implémentable : c'est documenté en §7 du cahier des charges.
    @PostMapping("/formateur")
    public ResponseEntity<PresenceResponse> ajouterParFormateur(
            @Valid @RequestBody AjouterPresenceFormateurRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presenceService.ajouterParFormateur(requete));
    }
}
