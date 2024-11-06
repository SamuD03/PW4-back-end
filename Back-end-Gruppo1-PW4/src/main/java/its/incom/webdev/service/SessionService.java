package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Session;
import its.incom.webdev.persistence.repository.SessionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.SQLException;

@ApplicationScoped
public class SessionService {

    private final SessionRepository sessionRepository;

    @Inject
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String findEmailBySessionId(String sessionId) {
        try {
            return sessionRepository.findEmailBySessionId(sessionId);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dell'email: " + e.getMessage(), e);
        }
    }
}
