package its.incom.webdev.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import its.incom.webdev.persistence.repository.EmailRepository;

import java.util.Optional;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    EmailRepository emailRepository;

    private static final String BASE_URL = "http://localhost:8080";
    public void sendVerificationEmail(String to, String token) {
        String userName = emailRepository.getUserNameByEmail(to);

        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }

        String verificationLink = BASE_URL + "/auth/confirm?token=" + token;
        String subject = "Email Verification";
        String textBody = "Hello " + userName + ",\n\nPlease verify your email by clicking the following link:\n" + verificationLink + "\n\nThank you!";
        String htmlBody = "<html><body><p>Hello " + userName + ",</p><p>Please verify your email by clicking the following link:</p><p><a href=\"" + verificationLink + "\">Verify Email</a></p><p>Thank you!</p></body></html>";

        Mail email = Mail.withText(to, subject, textBody)
                .setHtml(htmlBody);
        mailer.send(email);
    }

    public Optional<String> getEmailByVerificationToken(String token) {
        return emailRepository.getEmailByVerificationToken(token);
    }

    public void storeVerificationToken(String email, String token) {
        emailRepository.storeVerificationToken(email, token);
    }

    public void deleteVerificationToken(String token) {
        emailRepository.deleteVerificationToken(token);
    }

    @Scheduled(every = "24h")
    public void cleanupExpiredTokens() {
        emailRepository.cleanupExpiredTokens();
    }

    public boolean isEmailVerified(Integer userId) {
        return emailRepository.isEmailVerified(userId);
    }
}