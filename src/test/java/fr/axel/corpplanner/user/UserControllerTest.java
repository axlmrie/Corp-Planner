package fr.axel.corpplanner.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.axel.corpplanner.email.EmailService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @MockitoBean private EmailService emailService;

    @Test
    @DisplayName("Patch /me : Succès")
    @WithMockUser(username = "jean@calip.fr")
    void shouldUpdateOwnProfile() throws Exception {
        userRepository.saveAndFlush(User.builder()
                .email("jean@calip.fr")
                .password("secret")
                .roles(new HashSet<>())
                .build());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Axel");

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Axel"));
    }

    @Test
    @DisplayName("Get /me : Succès")
    @WithMockUser(username = "pro@calip.fr")
    void shouldGetMe() throws Exception {
        userRepository.saveAndFlush(User.builder()
                .email("pro@calip.fr")
                .password("password")
                .roles(new HashSet<>())
                .build());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("pro@calip.fr"));
    }

    @Test
    @DisplayName("Get /me : Erreur si l'utilisateur en session n'existe plus en base")
    @WithMockUser(username = "fantome@calip.fr")
    void shouldFailIfUserNotFoundInDb() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/users/me")))
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Doit rejeter l'accès si non authentifié")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}