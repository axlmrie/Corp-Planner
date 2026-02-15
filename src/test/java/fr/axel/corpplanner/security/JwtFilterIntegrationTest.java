package fr.axel.corpplanner.security;

import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
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

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    @Test
    void filterShouldAuthenticateWithValidTokenInCookie() throws Exception {
        User user = userRepository.save(User.builder()
                .email("auth@test.com")
                .password("encoded")
                .roles(new HashSet<>())
                .enabled(true)
                .build());

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/bookings/mine")
                        .cookie(new Cookie("accessToken", token)))
                .andExpect(status().isOk());
    }

    @Test
    void filterShouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/mine")
                        .cookie(new Cookie("accessToken", "invalid-token")))
                .andExpect(status().isUnauthorized());
    }
}