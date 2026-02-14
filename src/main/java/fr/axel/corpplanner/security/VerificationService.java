package fr.axel.corpplanner.security;

import fr.axel.corpplanner.config.InvalidCodeException;
import fr.axel.corpplanner.security.domain.TokenType;
import fr.axel.corpplanner.security.domain.VerificationToken;
import fr.axel.corpplanner.security.repository.VerificationTokenRepository;
import fr.axel.corpplanner.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;

    @Transactional
    public String createToken(User user, TokenType type) {
        tokenRepository.deleteByUserIdAndType(user.getId(), type);

        String code = String.format("%06d", new Random().nextInt(999999));

        int minutes = (type == TokenType.PASSWORD_RESET) ? 15 : 1440;

        VerificationToken vt = VerificationToken.builder()
                .token(code)
                .type(type)
                .expiryDate(LocalDateTime.now().plusMinutes(minutes))
                .build();

        tokenRepository.save(vt);
        return code;
    }

    public User validateToken(String code, TokenType type) {
        VerificationToken vt = tokenRepository.findByTokenAndType(code, type)
                .orElseThrow(() -> new InvalidCodeException("Code invalide ou inconnu"));

        if (vt.isExpired()) {
            tokenRepository.delete(vt);
            throw new InvalidCodeException("Le code a expiré, veuillez en demander un nouveau");
        }
        return vt.getUser();
    }

    @Transactional
    public void deleteToken(String code, TokenType type) {
        tokenRepository.findByTokenAndType(code, type).ifPresent(tokenRepository::delete);
    }
}