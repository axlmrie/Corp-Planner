package fr.axel.corpplanner.auth;

import fr.axel.corpplanner.email.EmailService;
import fr.axel.corpplanner.security.JwtService;
import fr.axel.corpplanner.security.VerificationService;
import fr.axel.corpplanner.security.domain.TokenType;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private VerificationService verificationService;
    @Mock private EmailService emailService;

    @InjectMocks private AuthenticationService authService;

    @Test
    @DisplayName("Inscription : Succès")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "Axel", "Du pont");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(verificationService.createToken(any(), any())).thenReturn("123456");

        String result = authService.register(request);

        assertThat(result).contains("Compte créé");
        verify(emailService).send(eq("test@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Inscription : Échec si email déjà utilisé")
    void registerFailEmailTaken() {
        RegisterRequest request = new RegisterRequest("A", "B", "taken@test.com", "pass");
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Activation : Succès")
    void confirmActivationSuccess() {
        User user = User.builder().email("test@test.com").enabled(false).build();
        when(verificationService.validateToken("123456", TokenType.ACTIVATION)).thenReturn(user);

        authService.confirmActivation("123456");

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(verificationService).deleteToken("123456", TokenType.ACTIVATION);
    }

    @Test
    @DisplayName("Oubli mot de passe : Email connu (RESET)")
    void requestPasswordReset_KnownEmail() {
        User user = User.builder().email("user@test.com").build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(verificationService.createToken(user, TokenType.PASSWORD_RESET)).thenReturn("654321");

        Map<String, String> response = authService.requestPasswordReset("user@test.com");

        assertThat(response.get("status")).isEqualTo("RESET");
        verify(emailService).send(eq("user@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Oubli mot de passe : Email inconnu (REGISTER)")
    void requestPasswordReset_UnknownEmail() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        Map<String, String> response = authService.requestPasswordReset("unknown@test.com");

        assertThat(response.get("status")).isEqualTo("REGISTER");
    }

    @Test
    @DisplayName("Reset Password : Échec si email ne correspond pas au token")
    void resetPassword_EmailMismatch() {
        User user = User.builder().email("vrai@test.com").build();
        when(verificationService.validateToken("123", TokenType.PASSWORD_RESET)).thenReturn(user);

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("pirate@test.com", "123", "newPass")
        );
    }

    @Test
    @DisplayName("Authentification : Succès")
    void authenticateSuccess() {
        AuthenticationRequest request = new AuthenticationRequest("test@test.com", "password");
        User user = User.builder().email("test@test.com").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt");

        AuthenticationResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt");
        verify(authenticationManager).authenticate(any());
    }
}