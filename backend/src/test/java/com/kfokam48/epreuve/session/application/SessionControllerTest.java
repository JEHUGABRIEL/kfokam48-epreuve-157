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

    @Test
    void cloturer_une_session_renvoie_200_avec_l_instant_de_cloture() throws Exception {
        long id = ouvrirUneSession();

        mockMvc.perform(post("/api/sessions/{id}/cloture", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.clotureAt", notNullValue()));
    }

    @Test
    void cloturer_une_session_deja_cloturee_renvoie_409_au_format_impose() throws Exception {
        long id = ouvrirUneSession();
        mockMvc.perform(post("/api/sessions/{id}/cloture", id)).andExpect(status().isOk());

        mockMvc.perform(post("/api/sessions/{id}/cloture", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void cloturer_une_session_inconnue_renvoie_404_au_format_impose() throws Exception {
        mockMvc.perform(post("/api/sessions/{id}/cloture", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    /** Ouvre une session et rend son identifiant, pour ne pas répéter la plomberie JSON. */
    private long ouvrirUneSession() throws Exception {
        String corps = mockMvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new OuvrirSessionRequest("Session démo", 1L))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corps).get("id").asLong();
    }
}
