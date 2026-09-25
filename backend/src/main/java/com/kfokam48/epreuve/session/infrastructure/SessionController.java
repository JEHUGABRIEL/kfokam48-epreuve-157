package com.kfokam48.epreuve.session.infrastructure;

import com.kfokam48.epreuve.session.application.SessionService;
import com.kfokam48.epreuve.session.application.dto.OuvrirSessionRequest;
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
}
