package fr.axel.corpplanner.email;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private MailjetClient mailjetClient;

    @Mock
    private MailjetResponse mailjetResponse;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService("fakeApiKey", "fakeSecretKey", "noreply@calip.fr");

        ReflectionTestUtils.setField(emailService, "mailjetClient", mailjetClient);
    }

    @Test
    @DisplayName("Devrait envoyer un e-mail avec succès (Status 200)")
    void shouldSendEmailSuccess() throws MailjetException {
        String to = "user@test.com";
        String subject = "Sujet Test";
        String content = "<h1>Contenu HTML</h1>";

        when(mailjetResponse.getStatus()).thenReturn(200);
        when(mailjetClient.post(any(MailjetRequest.class))).thenReturn(mailjetResponse);

        assertDoesNotThrow(() -> emailService.send(to, subject, content));

        verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
    }

    @Test
    @DisplayName("Devrait lever une RuntimeException si MailjetException survient")
    void shouldThrowExceptionOnMailjetError() throws MailjetException {
        when(mailjetClient.post(any(MailjetRequest.class))).thenThrow(new MailjetException("Erreur API Mailjet"));

        assertThrows(RuntimeException.class, () ->
                emailService.send("to@test.com", "sub", "content")
        );
    }
}