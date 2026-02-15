package fr.axel.corpplanner.security;

import fr.axel.corpplanner.security.domain.TokenType;
import fr.axel.corpplanner.security.domain.VerificationToken;
import fr.axel.corpplanner.security.repository.VerificationTokenRepository;
import fr.axel.corpplanner.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock private VerificationTokenRepository tokenRepository;
    @InjectMocks private VerificationService verificationService;

    @Test
    @DisplayName("Devrait créer un token de 6 chiffres")
    void shouldCreateToken() {
        User user = User.builder().id(1L).email("test@test.com").build();

        String code = verificationService.createToken(user, TokenType.ACTIVATION);

        assertThat(code).hasSize(6);
        assertThat(code).containsOnlyDigits();
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    @DisplayName("Devrait valider un token existant")
    void shouldValidateToken() {
        User user = User.builder().id(1L).email("test@test.com").build();
        VerificationToken vt = VerificationToken.builder()
                .token("123456")
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByTokenAndType("123456", TokenType.ACTIVATION))
                .thenReturn(Optional.of(vt));

        User result = verificationService.validateToken("123456", TokenType.ACTIVATION);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }
}