package fr.axel.corpplanner.auth;

import fr.axel.corpplanner.email.EmailService;
import fr.axel.corpplanner.security.JwtService;
import fr.axel.corpplanner.security.VerificationService;
import fr.axel.corpplanner.security.domain.TokenType;
import fr.axel.corpplanner.user.domain.Role;
import fr.axel.corpplanner.user.domain.User;
import fr.axel.corpplanner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final EmailService emailService;


    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(new HashSet<>(Set.of(Role.EMPLOYEE)))
                .enabled(false)
                .build();

        userRepository.save(user);

        String code = verificationService.createToken(user, TokenType.ACTIVATION);
        String htmlContent = "<h2>Bienvenue !</h2><p>Votre code d'activation est : <b>" + code + "</b></p>";
        emailService.send(user.getEmail(), "Activation de votre compte", htmlContent);

        return "Compte créé. Veuillez l'activer avec le code envoyé par e-mail.";
    }


    @Transactional
    public void confirmActivation(String code) {
        User user = verificationService.validateToken(code, TokenType.ACTIVATION);
        user.setEnabled(true);
        userRepository.save(user);
        verificationService.deleteToken(code, TokenType.ACTIVATION);
    }


    @Transactional
    public Map<String, String> requestPasswordReset(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String code = verificationService.createToken(user, TokenType.PASSWORD_RESET);
                    emailService.send(email, "Réinitialisation de mot de passe", "Votre code est : " + code);
                    return Map.of(
                            "status", "RESET",
                            "message", "Un code a été envoyé à votre adresse."
                    );
                })
                .orElseGet(() -> {
                    return Map.of(
                            "status", "REGISTER",
                            "message", "Cette adresse email est inconnue. Souhaitez-vous créer un compte ?"
                    );
                });
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = verificationService.validateToken(code, TokenType.PASSWORD_RESET);

        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("Ce code ne correspond pas à cet utilisateur.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        verificationService.deleteToken(code, TokenType.PASSWORD_RESET);
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}