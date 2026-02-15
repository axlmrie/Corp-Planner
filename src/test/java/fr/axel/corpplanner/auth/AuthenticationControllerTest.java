package fr.axel.corpplanner.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.email.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthenticationService authService;
    @MockitoBean private EmailService emailService;

    @Test
    @DisplayName("Login : Doit retourner un cookie accessToken")
    void loginShouldReturnCookie() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("test@test.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("fake-jwt-token");

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(jsonPath("$.message").value("Connexion réussie"));
    }

    @Test
    @DisplayName("Logout : Doit supprimer le cookie")
    void logoutShouldClearCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0));
    }

    @Test
    @DisplayName("Activate : Doit appeler le service")
    void activateShouldWork() throws Exception {
        mockMvc.perform(post("/api/v1/auth/activate")
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("activé")));
        verify(authService).confirmActivation("123456");
    }

    @Test
    @DisplayName("Forgot Password : Doit renvoyer la map du service")
    void forgotPasswordShouldWork() throws Exception {
        when(authService.requestPasswordReset("test@test.com"))
                .thenReturn(Map.of("status", "RESET", "message", "ok"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESET"));
    }
}