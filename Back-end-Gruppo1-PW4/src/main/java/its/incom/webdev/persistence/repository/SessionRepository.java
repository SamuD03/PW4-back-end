package its.incom.webdev.persistence.repository;

import its.incom.webdev.persistence.model.Session;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionRepository {

    private final DataSource database;

    // Costruttore con iniezione delle dipendenze
    public SessionRepository(DataSource database) {
        this.database = database;
    }

    // Metodo per inserire una nuova sessione nel database
    public String createSession(String email) throws SQLException {
        String sessionId = UUID.randomUUID().toString(); // Generate a new GUID

        try (Connection connection = database.getConnection()) {
            String query = "INSERT INTO session (id, email) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, sessionId); // Set the GUID as the session ID
                statement.setString(2, email); // Set the email for the session
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
                        String email = resultSet.getString("email");
                        return new Session(id, email);
                    } else {
                        return null; // Sessione non trovata
                    }
                }
            }
        }
    }
    public boolean sessionExists(String email) throws SQLException {
        try(Connection c = database.getConnection()) {
            String query = "SELECT COUNT(*) FROM session WHERE email=?";
            try(PreparedStatement statement = c.prepareStatement(query)) {
                statement.setString(1, email);
                try(ResultSet rs = statement.executeQuery()) {
                    if(rs.next()){
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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
}