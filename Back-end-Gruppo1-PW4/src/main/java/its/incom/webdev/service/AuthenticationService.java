package its.incom.webdev.service;

import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.service.exception.ExistingSessionException;
import its.incom.webdev.service.exception.SessionCreationException;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@ApplicationScoped
public class AuthenticationService {

    @Inject
    DataSource database;

    private final UserRepository userRepository;
    private final UserService userService;
    private final HashCalculator hashCalculator;
    private final SessionRepository sessionRepository;

    public AuthenticationService(
            UserRepository userRepository,
            UserService userService,
            HashCalculator hashCalculator,
            SessionRepository sessionRepository
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.hashCalculator = hashCalculator;
        this.sessionRepository = sessionRepository;
    }

    public String login(String email, String password, String phoneNumber) throws WrongUsernameOrPasswordException, SessionCreationException, ExistingSessionException {
        // Calculate the password hash
        String hash = hashCalculator.calculateHash(password);

        // attempt to find the user using email or phone number
        Optional<User> maybeUser = userRepository.findByEmailOrNumber(email, phoneNumber);

        // user found?
        if (maybeUser.isEmpty()) {
            throw new WrongUsernameOrPasswordException();
        }

        User user = maybeUser.get();

        // Check if the provided password hash matches the stored hash
        if (!user.getPswHash().equals(hash)) {
            throw new WrongUsernameOrPasswordException();
        }

        try {
            // session alr exist?
            if (sessionRepository.sessionExists(String.valueOf(user.getId()))) {
                throw new ExistingSessionException("Session already exists for user ID: " + user.getId());
            }

            // create a new session for the user using the user ID
            return sessionRepository.createSession(user.getId());
        } catch (SQLException e) {
            throw new SessionCreationException(e);
        }
    }

    public void delete(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new RuntimeException("SessionId must be provided");
        }
        sessionRepository.deleteSession(sessionId);
    }

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
