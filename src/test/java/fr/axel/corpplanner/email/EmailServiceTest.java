package fr.axel.corpplanner.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@calip.fr");
    }

    @Test
    @DisplayName("Devrait envoyer un e-mail avec succès")
    void shouldSendEmailSuccess() {
        // GIVEN
        String to = "user@test.com";
        String subject = "Sujet Test";
        String content = "<h1>Contenu HTML</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // WHEN
        emailService.send(to, subject, content);

        // THEN
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Devrait lever une RuntimeException si MessagingException survient")
    void shouldThrowExceptionOnMessagingError() {
        // GIVEN
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // On simule une erreur lors de la préparation de l'email (setFrom par exemple)
        // Mais comme MimeMessageHelper est difficile à mocker directement,
        // on peut aussi simuler l'erreur au moment du send()
        doAnswer(invocation -> {
            throw new MessagingException("Erreur SMTP");
        }).when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () ->
                emailService.send("to@test.com", "sub", "content")
        );
    }
}