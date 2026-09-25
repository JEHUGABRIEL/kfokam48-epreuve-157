package com.kfokam48.epreuve.common.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie B4 sur le conseil d'erreurs lui-même : toute erreur sort en {@code { code, message }}.
 *
 * <p>Le test monte un MockMvc autonome avec un contrôleur dédié plutôt qu'un
 * {@code @SpringBootTest} : le contexte complet ne démarre pas encore, parce que les modules
 * presence/exercice/relecture sont à l'état de squelette. S'appuyer sur eux rendrait ce test
 * rouge pour une raison qui n'a rien à voir avec le format d'erreur — or c'est justement la
 * brique dont tous les autres modules dépendent pour être vérifiables.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void preparerLeConseilDErreurs() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControleurDeTest())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Une erreur métier garde SON code et SON statut : c'est ainsi que les codes imposés par le
    // contrat (CODE_EXPIRE, DEJA_PRESENT, ...) arrivent jusqu'au client.
    @Test
    void une_erreur_metier_garde_son_code_et_son_statut() throws Exception {
        mockMvc.perform(post("/test/metier"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CODE_TEST"))
                .andExpect(jsonPath("$.message").value("Erreur métier de test."));
    }

    @Test
    void un_champ_obligatoire_manquant_renvoie_400_au_format_impose() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Le champ « titre » est obligatoire ou invalide."));
    }

    @Test
    void un_corps_illisible_renvoie_400_au_format_impose() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content("{ ceci n'est pas du JSON"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ENF3 : une panne interne ne doit laisser fuir ni stack trace, ni requête SQL, ni nom de classe.
    @Test
    void une_erreur_imprevue_renvoie_500_sans_fuite_technique() throws Exception {
        mockMvc.perform(post("/test/panne"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue. Réessayez plus tard."));
    }

    /** Erreur métier de test : porte un code et un statut, comme n'importe quelle ApiException. */
    static class ErreurDeTest extends ApiException {
        ErreurDeTest() {
            super("CODE_TEST", HttpStatus.CONFLICT, "Erreur métier de test.");
        }
    }

    record RequeteDeTest(@NotBlank String titre) {
    }

    /**
     * Contrôleur dédié au test : il ne sert qu'à faire lever au conseil d'erreurs les quatre
     * familles d'exceptions qu'il doit traiter.
     */
    @RestController
    @RequestMapping("/test")
    static class ControleurDeTest {

        @PostMapping("/metier")
        ResponseEntity<Void> metier() {
            throw new ErreurDeTest();
        }

        @PostMapping("/validation")
        ResponseEntity<Void> validation(@Valid @RequestBody RequeteDeTest requete) {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/panne")
        ResponseEntity<Void> panne() {
            throw new IllegalStateException("panne interne simulée");
        }
    }
}
