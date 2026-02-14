package fr.axel.corpplanner.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Devrait générer un token valide et extraire le username")
    void shouldGenerateAndExtractUsername() {
        // GIVEN
        UserDetails userDetails = new User("warrior@calip.fr", "password", new ArrayList<>());

        // WHEN
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);

        // THEN
        assertThat(extractedUsername).isEqualTo("warrior@calip.fr");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("Le token ne devrait pas être valide pour un autre utilisateur")
    void shouldNotBeValidForOtherUser() {
        UserDetails user1 = new User("user1@calip.fr", "password", new ArrayList<>());
        UserDetails user2 = new User("user2@calip.fr", "password", new ArrayList<>());

        String tokenUser1 = jwtService.generateToken(user1);

        assertThat(jwtService.isTokenValid(tokenUser1, user2)).isFalse();
    }
}