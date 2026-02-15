package fr.axel.corpplanner.email;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final MailjetClient mailjetClient;
    private final String fromEmail;

    public EmailService(
            @Value("${mailjet.api-key}") String apiKey,
            @Value("${mailjet.secret-key}") String secretKey,
            @Value("${application.mail.from}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(secretKey)
                .build();

        this.mailjetClient = new MailjetClient(options);
    }

    @Async
    public void send(String to, String subject, String content) {
        try {
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                            .put(Emailv31.Message.FROM, new JSONObject()
                                                    .put("Email", fromEmail)
                                                    .put("Name", "Corp Planner"))
                                            .put(Emailv31.Message.TO, new JSONArray()
                                                    .put(new JSONObject()
                                                            .put("Email", to)))
                                            .put(Emailv31.Message.SUBJECT, subject)
                                            .put(Emailv31.Message.HTMLPART, content)
                            ));

            MailjetResponse response = mailjetClient.post(request);

            if (response.getStatus() == 200) {
                log.info("E-mail envoyé avec succès à {}", to);
            } else {
                log.error("Erreur Mailjet: Statut {} - Données: {}", response.getStatus(), response.getData());
            }

        } catch (MailjetException e) {
            log.error("Exception lors de l'envoi de l'e-mail à {}", to, e);
            throw new RuntimeException("Erreur critique lors de l'envoi via Mailjet");
        }
    }
}