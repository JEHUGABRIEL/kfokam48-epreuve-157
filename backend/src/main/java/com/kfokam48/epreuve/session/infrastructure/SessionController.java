package com.kfokam48.epreuve.session.infrastructure;

import com.kfokam48.epreuve.session.application.SessionService;
import com.kfokam48.epreuve.session.application.dto.OuvrirSessionRequest;
import com.kfokam48.epreuve.session.application.dto.SessionClotureeResponse;
import com.kfokam48.epreuve.session.application.dto.SessionOuverteResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // EF2 / RG1 — POST /api/sessions
    @PostMapping
    public ResponseEntity<SessionOuverteResponse> ouvrir(@Valid @RequestBody OuvrirSessionRequest requete) {
        SessionOuverteResponse reponse = sessionService.ouvrir(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    // EF8 / RG13 — POST /api/sessions/{id}/cloture
    // Hors contrat imposé : l'Annexe B ne prévoyait aucune opération de clôture, alors que
    // Q3, Q10, Q11, Q12 et Q15 en dépendent toutes (cf. cahier des charges section 7).
    @PostMapping("/{id}/cloture")
    public ResponseEntity<SessionClotureeResponse> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.cloturer(id));
    }
}
