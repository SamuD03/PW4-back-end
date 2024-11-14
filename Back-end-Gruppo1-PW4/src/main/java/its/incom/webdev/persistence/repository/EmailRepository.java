package its.incom.webdev.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class EmailRepository {

    @Inject
    DataSource dataSource;

    public String getUserNameByEmail(String email) {
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
            e.printStackTrace();
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
            e.printStackTrace();
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