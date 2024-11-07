package its.incom.webdev.persistence.repository;

import its.incom.webdev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    @Inject
    DataSource dataSource;
    private final DataSource database;

    public UserRepository(DataSource database) {
        this.database = database;
    }

    public User createUtente(User user) throws SQLException {
        if (checkUtente(user.getEmail(), user.getPswHash())) {
            throw new BadRequestException("Utente già esistente");
        }

        String query = "INSERT INTO user (name, surname, email, pswHash, admin, verified) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setBoolean(5, user.isAdmin());
            statement.setBoolean(6, user.isVerified());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la creazione dell'utente", e);
        }

        return user;
    }

    private boolean checkUtente(String email, String pswHash) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ? AND pswHash = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, pswHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante il controllo dell'utente", e);
        }
    }

    public Optional<User> findByEmailPsw(String email, String pswHash) {
        String query = "SELECT email, pswHash FROM user WHERE email = ? AND pswHash = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, pswHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String query = "SELECT email, pswHash, verified FROM user WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    user.setVerified(resultSet.getBoolean("verified"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> getUtenteByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    user.setAdmin(resultSet.getBoolean("admin"));
                    user.setVerified(resultSet.getBoolean("verified"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la ricerca dell'utente", e);
        }
        return Optional.empty();
    }

    public List<User> getUser() {
        List<User> list = new ArrayList<>();
        String query = "SELECT name, surname, email, admin FROM user";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setAdmin(resultSet.getBoolean("admin"));
                    list.add(user);
                }
                return list;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la selezione degli utenti", e);
        }
    }

    public void updateVerified(String email, boolean verified) {
        String query = "UPDATE user SET verified = ? WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, verified);
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating verified status", e);
        }
    }

    public boolean checkAdmin(String email) throws SQLException {
        String query = "SELECT admin FROM user WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("admin");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nel controllo del ruolo admin", e);
        }
        return false;
    }

    public Optional<User> findByNumber(String phoneNumber) {
        String query = "SELECT * FROM user WHERE number = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, phoneNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setEmail(resultSet.getString("email"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    user.setNumber(resultSet.getString("number"));
                    user.setAdmin(resultSet.getBoolean("admin"));
                    user.setVerified(resultSet.getBoolean("verified"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by phone number", e);
        }
        return Optional.empty();
    }

    public User create(User user) {
        // Updated query to include the number field
        String query = "INSERT INTO user (name, surname, email, pswHash, number, admin, verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setString(5, user.getNumber()); // Added number field
            statement.setBoolean(6, user.isAdmin());
            statement.setBoolean(7, user.isVerified());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la creazione dell'utente", e);
        }
        return user;
    }
    public void updateVerifiedWithPhone(String phoneNumber, boolean verified) {
        String query = "UPDATE user SET verified = ? WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, verified);
            statement.setString(2, phoneNumber);

            int rowsAffected = statement.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected); // Log the number of rows affected

            if (rowsAffected == 0) {
                throw new RuntimeException("No rows updated. The phone number might be incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating verified status with phone", e);
        }
    }

}
