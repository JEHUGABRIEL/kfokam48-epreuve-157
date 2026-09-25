package com.kfokam48.epreuve.session.application;

import com.kfokam48.epreuve.session.application.dto.OuvrirSessionRequest;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // EF2 : quand je crée une session avec titre + promotionId, je reçois id, code, ouvertureAt, expirationAt.
    @Test
    void ouvrir_une_session_renvoie_201_avec_le_code() throws Exception {
        OuvrirSessionRequest requete = new OuvrirSessionRequest("Session démo", 1L);

        mockMvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requete)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", notNullValue()))
                .andExpect(jsonPath("$.ouvertureAt", notNullValue()))
                .andExpect(jsonPath("$.expirationAt", notNullValue()));
    }

    // B4 : un corps de requête invalide renvoie le format d'erreur imposé, jamais de stack trace.
    @Test
    void ouvrir_une_session_sans_titre_renvoie_400_au_format_impose() throws Exception {
        String corpsInvalide = "{ \"promotionId\": 1 }";

        mockMvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content(corpsInvalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()));
    }
}
