package its.incom.webdev.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@ApplicationScoped
public class AuthenticationRepository {

    @Inject
    DataSource database;

    public Optional<String> getSessionIdByEmailOrPhone(String email, String phoneNumber) {
        String query = "SELECT s.id FROM session s " +
                "JOIN user u ON s.user_id = u.id " +
                "WHERE u.email = ? OR u.number = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, phoneNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve session ID", e);
        }

        return Optional.empty();
    }
}