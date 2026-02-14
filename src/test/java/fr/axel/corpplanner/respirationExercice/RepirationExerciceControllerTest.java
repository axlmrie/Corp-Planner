package fr.axel.corpplanner.respirationExercice;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.config.SecurityConfiguration;
import fr.calip.script_manager.respirationExercice.domain.ParametreResipiration;
import fr.axel.corpplanner.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepirationExerciceController.class)
@Import(SecurityConfiguration.class)
@DisplayName("Integration Tests - RepirationExerciceController (Web Layer)")
class RepirationExerciceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private RespirationExerciceService service;

    // --- MOCKS DE SÉCURITÉ ---
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider; // C'ÉTAIT LUI LE COUPABLE !

    @Test
    @WithMockUser(authorities = "EXERCICE_ADMIN")
    @DisplayName("POST /cree - Succès avec le rôle ADMIN")
    void createExercise_Success() throws Exception {
        // Given
        var param = ParametreResipiration.builder()
                .id(1L)
                .nomPreset("Zen")
                .respiration(5)
                .apnee(2)
                .expiration(5)
                .build();

        when(service.createParametreResipiration(any(ParametreResipiration.class))).thenReturn(param);

        // When & Then
        mockMvc.perform(post("/api/v1/RepirationExerciceController/cree")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(param)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomPreset").value("Zen"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET / - Succès pour utilisateur authentifié")
    void getAll_Success() throws Exception {
        // Given
        when(service.getAllParametreResipirations()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/RepirationExerciceController"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(authorities = "USER")
    @DisplayName("POST /cree - Interdit pour un simple USER")
    void createExercise_Forbidden() throws Exception {
        var param = new ParametreResipiration();

        mockMvc.perform(post("/api/v1/RepirationExerciceController/cree")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(param)))
                .andExpect(status().isForbidden());
    }
}