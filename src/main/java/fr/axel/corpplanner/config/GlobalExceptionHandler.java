package fr.axel.corpplanner.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Structure unique pour toutes les erreurs de l'API
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            Object details
    ) {}

    /**
     * 400 - Erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "Certains champs sont invalides", errors);
    }

    /**
     * 404 - Ressource non trouvée (Script, User, etc.)
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    /**
     * 403 - Droits insuffisants
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Vous n'avez pas les permissions nécessaires", null);
    }

    /**
     * 401 - Authentification ratée
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Email ou mot de passe incorrect", null);
    }

    /**
     * 500 - Erreurs inattendues (Catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Erreur non gérée : ", ex); // On logue l'erreur complète pour nous
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Une erreur technique est survenue, veuillez contacter l'administrateur", // Message sécurisé
                null
        );
    }
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCode(InvalidCodeException ex) {
        // On crée la map "details" pour que React sache quel champ souligner
        Map<String, String> details = Map.of("code", ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST, // On force le code 400
                "Invalid Code",
                ex.getMessage(),
                details
        );
    }
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, Object details) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                details
        ));
    }
}