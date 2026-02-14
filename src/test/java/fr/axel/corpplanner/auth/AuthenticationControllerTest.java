package fr.axel.corpplanner.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.security.repository.VerificationTokenRepository;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Utilise H2 au lieu de ton MySQL de dev
@Transactional // Remet la base à zéro après chaque test
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;
    @MockitoBean
    private JavaMailSender mailSender;
    @Test
    @DisplayName("Cycle complet : Inscription -> Activation -> Login")
    void registerAndLoginSuccess() throws Exception {
        var req = new RegisterRequest("test@calip.fr", "password123", "test", "test", "test", "test","test");

        // 1. Inscription
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // 2. Récupérer le code
        String code = tokenRepository.findAll().get(0).getToken();

        // 3. Activation
        mockMvc.perform(post("/api/v1/auth/activate")
                        .param("code", code)) // Envoyé en @RequestParam
                .andExpect(status().isOk());

        // 4. Login
        var login = new AuthenticationRequest("test@calip.fr", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"));
    }

    @Test
    @DisplayName("Refuser la connexion si le mot de passe est faux")
    void loginFailureWrongPassword() throws Exception {
        // Création et activation rapide
        registerAndLoginSuccess();

        var badAuthRequest = new AuthenticationRequest("newuser@calip.fr", "wrong_pass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badAuthRequest)))
                .andExpect(status().isUnauthorized()); // Doit renvoyer 401
    }


    @Test
    @DisplayName("Refuser l'inscription si l'email existe déjà")
    void registerFailureDuplicateEmail() throws Exception {
        var request = new RegisterRequest("duplicate@calip.fr", "password","test", "test", "test", "test","test");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}