package com.kfokam48.epreuve.relecture.infrastructure;

import com.kfokam48.epreuve.relecture.application.RelectureService;
import com.kfokam48.epreuve.relecture.application.dto.RelectureAssigneeResponse;
import com.kfokam48.epreuve.relecture.application.dto.RelectureResponse;
import com.kfokam48.epreuve.relecture.application.dto.RendreRelectureRequest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapte le module relecture au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code POST /api/relectures/{id}} → 200 ; 400 {@code NOTE_INVALIDE} ; 403
 *       {@code AUTO_RELECTURE} ; 409 {@code RELECTURE_DEJA_RENDUE}</li>
 *   <li>{@code GET /api/relectures/assignees?relecteurId=} → 200 (hors contrat imposé, UC7)</li>
 *   <li>{@code GET /api/relectures/recues?etudiantId=&sessionId=} → 200, 404
 *       {@code RELECTURE_INCONNUE} (hors contrat imposé, EF7 / RG6)</li>
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

    // UC7 — le relecteur découvre ce qu'il doit rendre, donc l'identifiant à passer ci-dessus.
    @GetMapping("/assignees")
    public ResponseEntity<List<RelectureAssigneeResponse>> assignees(@RequestParam Long relecteurId) {
        return ResponseEntity.ok(relectureService.listerAssignees(relecteurId));
    }

    // EF7 / RG6 — l'étudiant relu consulte sa note, sans jamais savoir qui l'a relu.
    @GetMapping("/recues")
    public ResponseEntity<RelectureResponse> recues(@RequestParam Long etudiantId,
                                                    @RequestParam Long sessionId) {
        return ResponseEntity.ok(relectureService.consulterRecue(etudiantId, sessionId));
    }
}
