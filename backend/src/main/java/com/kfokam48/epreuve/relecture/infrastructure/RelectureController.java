package com.kfokam48.epreuve.relecture.infrastructure;

import com.kfokam48.epreuve.relecture.application.RelectureService;
import com.kfokam48.epreuve.relecture.application.dto.RelectureResponse;
import com.kfokam48.epreuve.relecture.application.dto.RendreRelectureRequest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapte le module relecture au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code POST /api/relectures/{id}} → 200 ; 400 {@code NOTE_INVALIDE} ; 403
 *       {@code AUTO_RELECTURE} ; 409 {@code RELECTURE_DEJA_RENDUE}</li>
 * </ul>
 * L'opération imposée n'impose aucun corps de réponse : la note et le commentaire sont renvoyés tels
 * qu'ils viennent d'être enregistrés, ce qui évite au relecteur un aller-retour pour les relire.
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    // EF6 — le relecteur rend sa note et son commentaire.
    @PostMapping("/{id}")
    public ResponseEntity<RelectureResponse> rendre(@PathVariable Long id,
                                                    @Valid @RequestBody RendreRelectureRequest requete) {
        return ResponseEntity.ok(relectureService.rendre(id, requete));
    }
}
