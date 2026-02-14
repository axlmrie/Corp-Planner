package fr.axel.corpplanner.security;

import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import jakarta.servlet.http.Cookie; // ✅ Indispensable pour MockMvc
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Le filtre doit authentifier l'utilisateur avec un cookie accessToken valide")
    void filterShouldAuthenticateWithValidToken() throws Exception {
        // 1. GIVEN : Un utilisateur en base et son token généré
        User user = userRepository.saveAndFlush(User.builder()
                .email("real@calip.fr")
                .password("pass")
                .roles(new HashSet<>())
                .build());

        String token = jwtService.generateToken(user);

        // 2. WHEN : On appelle la route avec le token dans un COOKIE
        mockMvc.perform(get("/api/v1/users/me")
                        .cookie(new Cookie("accessToken", token))) // ✅ Changement ici

                // 3. THEN : 200 OK car l'utilisateur est reconnu
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Le filtre doit rejeter (401) une requête avec un cookie invalide")
    void filterShouldRejectInvalidToken() throws Exception {
        // WHEN : On envoie un cookie corrompu
        mockMvc.perform(get("/api/v1/users/me")
                        .cookie(new Cookie("accessToken", "mauvais-token"))) // ✅ Changement ici

                // THEN : 401 car Spring Security ne trouve pas d'auth valide dans le contexte
                .andExpect(status().isUnauthorized());
    }
}