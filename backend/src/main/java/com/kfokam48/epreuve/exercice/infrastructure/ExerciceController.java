package com.kfokam48.epreuve.exercice.infrastructure;

import com.kfokam48.epreuve.exercice.application.ExerciceService;
import com.kfokam48.epreuve.exercice.application.dto.DeposerExerciceRequest;
import com.kfokam48.epreuve.exercice.application.dto.ExerciceDeposeResponse;
import com.kfokam48.epreuve.exercice.application.dto.RemplacerLienRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapte le module exercice au monde extérieur (contrat {@code api/contrat.yaml}).
 * <ul>
 *   <li>{@code POST /api/exercices} → 201 ; 400 {@code LIEN_INVALIDE} ; 409
 *       {@code EXERCICE_DEJA_DEPOSE}</li>
 *   <li>{@code PUT /api/exercices/{id}} → 200 ; 400 {@code LIEN_INVALIDE} ; 404
 *       {@code EXERCICE_INCONNU} ; 409 {@code RELECTURE_DEJA_RENDUE}</li>
 * </ul>
 * Les autres codes possibles (404 {@code SESSION_INCONNUE}, 404 {@code ETUDIANT_INCONNU}, 409
 * {@code SESSION_DEJA_CLOTUREE}) viennent des références que l'opération doit valider ; ils sont
 * documentés dans le contrat et en §7.
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    // EF3 — l'étudiant dépose le lien de son exercice pour une session.
    @PostMapping
    public ResponseEntity<ExerciceDeposeResponse> deposer(@Valid @RequestBody DeposerExerciceRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciceService.deposer(requete));
    }

    // EF4 / RG10 — remplacer le lien tant que la relecture n'est pas rendue.
    @PutMapping("/{id}")
    public ResponseEntity<ExerciceDeposeResponse> remplacerLien(@PathVariable Long id,
                                                                @Valid @RequestBody RemplacerLienRequest requete) {
        return ResponseEntity.ok(exerciceService.remplacerLien(id, requete));
    }
}
