package its.incom.webdev.persistence.repository;

import its.incom.webdev.persistence.model.Session;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

import its.incom.webdev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionRepository {

    private final DataSource database;

    // Costruttore con iniezione delle dipendenze
    public SessionRepository(DataSource database) {
        this.database = database;
    }

    // Metodo per inserire una nuova sessione nel database
    public String createSession(int userId) throws SQLException {
        String sessionId = UUID.randomUUID().toString(); // Generate a new GUID

        try (Connection connection = database.getConnection()) {
            String query = "INSERT INTO session (id, user_id) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId); // Set the GUID as the session ID
                statement.setInt(2, userId); // Set the user ID for the session
                statement.executeUpdate(); // Execute the insert
            }
        }
        return sessionId; // Return the generated session ID
    }


    // Metodo per eliminare una sessione dal database
    public void deleteSessione(String sessionId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "DELETE FROM session WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Cancellazione della sessione fallita, nessuna riga eliminata.");
                }
            }
        }
    }

    // Metodo per ottenere una sessione dal database tramite il suo ID
    public Session getSessionById(String sessionId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT * FROM session WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String id = resultSet.getString("id");
                        int userId = resultSet.getInt("user_id");

                        // Load the User entity using a custom method
                        User user = getUserById(userId);
                        if (user == null) {
                            throw new SQLException("User not found for user_id: " + userId);
                        }

                        return new Session(id, user);
                    } else {
                        return null; // Session not found
                    }
                }
            }
        }
    }

    // Custom method to retrieve a User by its ID
    private User getUserById(int userId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT * FROM user WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Assuming User has a constructor that takes ResultSet
                        return new User(resultSet);
                    } else {
                        return null; // User not found
                    }
                }
            }
        }
    }

    public boolean sessionExists(String contactInfo) {
        // Assuming your 'session' table has a 'user_id' column that links to the 'user' table
        String query = "SELECT COUNT(*) FROM session WHERE user_id = (SELECT id FROM user WHERE email = ? OR number = ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, contactInfo);
            statement.setString(2, contactInfo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Check if any sessions exist
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error checking session existence", e);
        }
        return false;
    }

    public String findEmailBySessionId(String sessionId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT email FROM session WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId); // Imposta il sessionId come parametro
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("email"); // Ritorna l'email associata
                    }
                }
            }
        }
        return null; // Ritorna null se nessuna sessione è trovata
    }

    public void deleteSession(String sessionId) {
        String query = "DELETE FROM session WHERE id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, sessionId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No session found with the provided sessionId: " + sessionId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error during session deletion: " + e.getMessage(), e);
        }
    }

    public Integer findUserIdBySessionId(String sessionId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String query = "SELECT user_id FROM session WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId); // Imposta il sessionId come parametro
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("user_id"); // Ritorna l'userId associato
                    }
                }
            }
        }
        return null; // Ritorna null se nessuna sessione è trovata
    }

}