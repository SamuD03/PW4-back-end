package its.incom.webdev.persistence.repository;

import its.incom.webdev.persistence.model.User;
import its.incom.webdev.rest.model.CreateUserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    public Optional<User> findByIdAndPsw(Integer userId, String pswHash) {
        String query = "SELECT id, pswHash FROM user WHERE id = ? AND pswHash = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setString(2, pswHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID and password", e);
        }
        return Optional.empty();
    }


    public Optional<User> findByEmailOrNumber(String email, String phoneNumber) {
        String query;
        boolean isEmailProvided = (email != null && !email.isEmpty());

        if (isEmailProvided) {
            query = "SELECT id, name, surname, email, pswHash, number, admin, verified FROM user WHERE email = ?";
        } else {
            query = "SELECT id, name, surname, email, pswHash, number, admin, verified FROM user WHERE number = ?";
        }

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (isEmailProvided) {
                statement.setString(1, email);
            } else {
                statement.setString(1, phoneNumber);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    user.setNumber(resultSet.getString("number"));
                    user.setAdmin(resultSet.getBoolean("admin"));
                    user.setVerified(resultSet.getBoolean("verified"));
                    return Optional.of(user);
                }
            }
            System.out.println("Email: " + email);
            System.out.println("Phone Number: " + phoneNumber);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by email or phone number", e);
        }
        return Optional.empty();
    }

    public List<CreateUserResponse> getFilteredUsers(boolean admin) throws SQLException {
        List<CreateUserResponse> list = new ArrayList<>();

        String query = "SELECT id, name, surname, email, number, admin, verified, notification FROM user WHERE admin = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, admin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CreateUserResponse user = new CreateUserResponse();
                    user.setId(resultSet.getInt("id")); // Set id
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setNumber(resultSet.getString("number"));
                    user.setAdmin(resultSet.getBoolean("admin")); // Set admin
                    user.setVerified(resultSet.getBoolean("verified")); // Set verified
                    user.setNotification(resultSet.getBoolean("notification")); // Set notification

                    list.add(user);
                }
            }
        }
        return list;
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

    public boolean checkAdmin(Integer userId) throws SQLException {
        String query = "SELECT admin FROM user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
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

    public User create(User user) {
        String query = "INSERT INTO user (name, surname, email, pswHash, number, admin, verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setString(5, user.getNumber());
            statement.setBoolean(6, user.isAdmin());
            statement.setBoolean(7, user.isVerified());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Failed to retrieve the generated ID for the new user.");
                }
            }
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
            System.out.println("Rows affected: " + rowsAffected);

            if (rowsAffected == 0) {
                throw new RuntimeException("No rows updated. The phone number might be incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating verified status with phone", e);
        }
    }

    public Optional<User> findNumber(String phoneNumber) {
        String query = "SELECT id, name, surname, email, pswHash, number, admin, verified FROM user WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, phoneNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
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

    public Optional<String> findPswHashByUserId(Integer userId) {
        String query = "SELECT pswHash FROM user WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("pswHash"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding password hash for user ID: " + userId, e);
        }
        return Optional.empty();
    }

    public boolean isAdmin(Integer userId) {
        String query = "SELECT admin FROM user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("admin");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error checking admin status", e);
        }
        return false;
    }

    public boolean updateNotificationPreference(Integer userId, boolean notification) {
        String checkEmailQuery = "SELECT email FROM user WHERE id = ?";
        String updateQuery = "UPDATE user SET notification = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkEmailQuery)) {
            checkStmt.setInt(1, userId);

            try (ResultSet resultSet = checkStmt.executeQuery()) {
                if (resultSet.next()) {
                    String email = resultSet.getString("email");
                    if (email != null && !email.isEmpty()) {
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setBoolean(1, notification);
                            updateStmt.setInt(2, userId);
                            int rowsAffected = updateStmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating notification preference", e);
        }
        return false;
    }

    public Optional<User> findById(int buyerId) {
        String query = "SELECT id, name, surname, email, pswHash, number, admin, verified, notification FROM user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    user.setNumber(resultSet.getString("number"));
                    user.setAdmin(resultSet.getBoolean("admin"));
                    user.setVerified(resultSet.getBoolean("verified"));
                    user.setNotification(resultSet.getBoolean("notification")); // Add this line
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by ID", e);
        }
        return Optional.empty();
    }


    public List<User> getAllAdmins() {
        String query = "SELECT id, name, surname, email FROM user WHERE admin = true";
        List<User> admins = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User admin = new User();
                admin.setId(resultSet.getInt("id"));
                admin.setName(resultSet.getString("name"));
                admin.setSurname(resultSet.getString("surname"));
                admin.setEmail(resultSet.getString("email"));
                admins.add(admin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving admin users", e);
        }
        return admins;
    }

    public boolean deleteUserById(int userId) {
        String query = "DELETE FROM user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public boolean updateUser(User user) {
        String query = "UPDATE user SET name = ?, surname = ?, email = ?, pswHash = ?, number = ?, admin = ?, verified = ?, notification = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setString(5, user.getNumber());
            statement.setBoolean(6, user.isAdmin());
            statement.setBoolean(7, user.isVerified());
            statement.setBoolean(8, user.isNotification());
            statement.setInt(9, user.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating user", e);
        }
    }
}
