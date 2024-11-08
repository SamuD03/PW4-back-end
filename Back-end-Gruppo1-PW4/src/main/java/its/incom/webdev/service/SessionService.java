package its.incom.webdev.service;

import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.SQLException;

@ApplicationScoped
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Inject
    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public Integer findUserIdBySessionId(String sessionId) {
        try {
            return sessionRepository.findUserIdBySessionId(sessionId);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del user_id: " + e.getMessage(), e);
        }
    }

    public String findPswHashBySessionId(String sessionId) {
        try {
            // retrieve the user ID from the session
            Integer userId = findUserIdBySessionId(sessionId);
            if (userId == null) {
                throw new RuntimeException("Invalid session or user ID not found");
            }

            // retrieve the user's password hash from the UserRepository
            return userRepository.findPswHashByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Password hash not found for user ID: " + userId));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving password hash: " + e.getMessage(), e);
        }
    }
}
