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

    // Costruttore con iniezione delle dipendenze
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

    // Metodo per effettuare il login
    public String login(String email, String password) throws WrongUsernameOrPasswordException, SessionCreationException, ExistingSessionException {
        // Calcola l'hash della password
        String hash = hashCalculator.calculateHash(password);
        // Cerca l'utente nel database
        Optional<User> maybePartecipante = userRepository.findByEmailPsw(email, hash);
        if (maybePartecipante.isPresent()) {
            User p = maybePartecipante.get();

            try {
                // Controlla se esiste già una sessione per l'utente
                if (sessionRepository.sessionExists(p.getEmail())) {
                    throw new ExistingSessionException("Session already exists for " + p.getEmail());
                }

                // Crea una nuova sessione per l'utente
                return sessionRepository.createSession(p.getEmail());

            } catch (SQLException e) {
                // Gestisce le eccezioni durante la verifica dell'esistenza della sessione
                throw new SessionCreationException(e);
            }
        } else {
            // Lancia un'eccezione se l'utente non esiste
            throw new WrongUsernameOrPasswordException();
        }
    }


    public CreateUserResponse getProfile(String sessionId) throws SQLException {
        //1. Recuperare la sessione dal database
        Session s = sessionRepository.getSessionById(sessionId);
        //2. Recuperare l'id partecipante della sessione
        String email = s.getEmail();
        //3. Recupero il partecipante dal database
        try {
            return userService.getUtenteByEmail(email);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String sessionId){
        if(sessionId == null || sessionId.isEmpty()) {
            throw new RuntimeException("SessionId must be provided");
        }
        try{
            sessionRepository.deleteSessione(sessionId);
        } catch (SQLException e) {
            throw new RuntimeException("errore durante la cancellazione" + e.getMessage());
        }
    }
    public Optional<User> getUserBySessionId(String sessionId) throws SQLException {
        // look into email in the session table by session_id
        String email = sessionRepository.findEmailBySessionId(sessionId);
        if (email != null) {
            // email to find user in the user table
            return userRepository.findByEmail(email);
        }
        return Optional.empty();
    }
    public void storeVerificationToken(String email, String token) {
        // token expiry 24hrs
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        String query = "INSERT INTO verification_token (email, token, expiry_date) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE token = ?, expiry_date = ?";

        try (Connection connection = database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, token);
                statement.setTimestamp(3, Timestamp.valueOf(expiryDate));
                statement.setString(4, token); // For update
                statement.setTimestamp(5, Timestamp.valueOf(expiryDate)); // For update

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store verification token", e);
        }
    }
}