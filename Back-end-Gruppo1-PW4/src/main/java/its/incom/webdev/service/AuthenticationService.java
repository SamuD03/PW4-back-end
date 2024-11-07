package its.incom.webdev.service;

import its.incom.webdev.rest.model.CreateUserResponse;
import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.model.Session;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.service.exception.ExistingSessionException;
import its.incom.webdev.service.exception.SessionCreationException;
import its.incom.webdev.service.exception.WrongUsernameOrPasswordException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    public CreateUserResponse getProfile(String sessionId) throws SQLException {
        // retrieve the session from the database
        Session session = sessionRepository.getSessionById(sessionId);

        if (session == null) {
            throw new RuntimeException("Session not found for the given sessionId");
        }

        // wse the user ID from the session to find the user
        Optional<User> maybeUser = userRepository.findByEmailOrNumber(session.getUser().getEmail(), session.getUser().getNumber());

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            return userService.getUtenteByEmail(user.getEmail());
        } else {
            throw new RuntimeException("User not found for the given session");
        }
    }

    public void delete(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new RuntimeException("SessionId must be provided");
        }
        sessionRepository.deleteSession(sessionId);
    }

    public Optional<User> getUserBySessionId(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new RuntimeException("SessionId must be provided");
        }

        // Retrieve the session from the session table using the session ID
        Session session = sessionRepository.getSessionById(sessionId);

        if (session == null) {
            throw new RuntimeException("Session not found for the provided sessionId");
        }

        // Use the user ID from the session to retrieve the user
        return Optional.of(session.getUser());
    }

    public void storeVerificationToken(String email, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        String query = "INSERT INTO verification_token (email, token, expiry_date) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE token = ?, expiry_date = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(expiryDate));
            statement.setString(4, token); // For update
            statement.setTimestamp(5, Timestamp.valueOf(expiryDate)); // For update

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store verification token", e);
        }
    }
}
