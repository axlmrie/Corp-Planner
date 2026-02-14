package fr.axel.corpplanner.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.dto.UserUpdateRequest;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

// IMPORT STATIQUES CRITIQUES (Version Servlet / MockMvc)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Un utilisateur connecté doit pouvoir modifier son propre profil sans ID")
    @WithMockUser(username = "jean@calip.fr")
    void shouldUpdateOwnProfile() throws Exception {
        // GIVEN : On crée l'utilisateur qui correspond au MockUser
        User me = userRepository.saveAndFlush(User.builder()
                .email("jean@calip.fr")
                .password("secret")
                .roles(new HashSet<>())
                .build());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("nouveau-email@calip.fr");

        // WHEN & THEN : On appelle /me au lieu de /1
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nouveau-email@calip.fr"));
    }

// Dans UserControllerTest.java

    @Test
    @DisplayName("Un simple USER doit pouvoir modifier son propre profil")
    @WithMockUser(username = "user@calip.fr", roles = {"USER"})
    void simpleUserUpdateOwnProfile() throws Exception {
        // GIVEN : On crée l'utilisateur en base
        userRepository.saveAndFlush(User.builder()
                .email("user@calip.fr")
                .password("pass")
                .roles(new HashSet<>())
                .build());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("nouveau@calip.fr");

        // WHEN & THEN : On appelle /me (qui existe) au lieu de /1 (qui n'existe plus)
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nouveau@calip.fr"));
    }

    @Test
    @DisplayName("Devrait rejeter l'accès au profil si non connecté (401)")
    void shouldRejectUnauthenticated() throws Exception {
        // Si pas de jeton, Spring Security renvoie 401
        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // ✅ Doit être Unauthorized (401)
    }


    @Test
    @DisplayName("Devrait récupérer le profil de l'utilisateur connecté")
    @WithMockUser(username = "pro@calip.fr")
    void shouldGetMe() throws Exception {
        // GIVEN
        User user = User.builder()
                .email("pro@calip.fr")
                .password("password")
                .roles(new HashSet<>())
                .build();

        userRepository.saveAndFlush(user);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("pro@calip.fr"));
    }



}