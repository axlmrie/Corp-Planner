package fr.axel.corpplanner.auth;

import fr.axel.corpplanner.email.EmailService;
import fr.axel.corpplanner.security.VerificationService;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.RoleRepository;
import fr.axel.corpplanner.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificationService verificationService;
    @Mock private EmailService emailService;

    @InjectMocks private AuthenticationService authService;

    @Test
    @DisplayName("Devrait lever une erreur si l'email est déjà pris")
    void shouldThrowExceptionWhenEmailExists() {
        var request = new RegisterRequest("alice@calip.fr", "password", "test", "test", "test", "test", "test");

        when(userRepository.findByEmail("alice@calip.fr")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }
}