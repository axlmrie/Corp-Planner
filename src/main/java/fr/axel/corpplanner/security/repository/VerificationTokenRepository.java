package fr.axel.corpplanner.security.repository;

import fr.axel.corpplanner.security.domain.VerificationToken;
import fr.axel.corpplanner.security.domain.TokenType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);
    @Modifying
    @Transactional
    void deleteByUserIdAndType(Long userId, TokenType type);
}