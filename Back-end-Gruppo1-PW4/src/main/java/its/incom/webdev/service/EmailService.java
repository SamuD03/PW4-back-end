package its.incom.webdev.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class EmailService {

    @Inject
    DataSource dataSource;

    @Inject
    Mailer mailer;

    private static final String BASE_URL = "http://localhost:8080"; // Configure this as needed

    public void sendVerificationEmail(String to, String token) {
        String userName = getUserNameByEmail(to);

        if (userName == null || userName.isEmpty()) {
            userName = "User"; // Default name if user not found
        }

        String verificationLink = BASE_URL + "/auth/confirm?token=" + token;
        String subject = "Email Verification";
        String textBody = "Hello " + userName + ",\n\nPlease verify your email by clicking the following link:\n" + verificationLink + "\n\nThank you!";
        String htmlBody = "<html><body><p>Hello " + userName + ",</p><p>Please verify your email by clicking the following link:</p><p><a href=\"" + verificationLink + "\">Verify Email</a></p><p>Thank you!</p></body></html>";

        Mail email = Mail.withText(to, subject, textBody)
                .setHtml(htmlBody);
        mailer.send(email);
    }

    private String getUserNameByEmail(String email) {
        String query = "SELECT name FROM user WHERE email = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead of printing stack trace
        }

        return null;
    }

    public Optional<String> getEmailByVerificationToken(String token) {
        String query = "SELECT email FROM verification_token WHERE token = ? AND expiry_date > CURRENT_TIMESTAMP";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging for production
        }

        return Optional.empty();
    }

    public void storeVerificationToken(String email, String token) {
        String query = "INSERT INTO verification_token (email, token, expiry_date) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusHours(24))); // Set expiry to 24 hours later
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteVerificationToken(String token) {
        String query = "DELETE FROM verification_token WHERE token = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(every = "24h")
    public void cleanupExpiredTokens() {
        String query = "DELETE FROM verification_token WHERE expiry_date <= CURRENT_TIMESTAMP";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            int deletedRows = statement.executeUpdate();
            System.out.println("Deleted expired tokens: " + deletedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isEmailVerified(Integer userId) {
        String query = "SELECT verified FROM user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("verified");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}

