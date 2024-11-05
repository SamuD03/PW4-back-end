package its.incom.webdev.service;


import its.incom.webdev.persistence.model.*;

import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.rest.model.CreateUserRequest;
import its.incom.webdev.rest.model.CreateUserResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final HashCalculator hashCalculator;

    // Costruttore con iniezione delle dipendenze
    public UserService(SessionRepository sessionRepository, UserRepository userRepository, HashCalculator hashCalculator) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.hashCalculator = hashCalculator;
    }

    // Metodo privato per convertire un oggetto Utente in un oggetto CreateUtenteResponse
    private CreateUserResponse convertToResponse(User user) {
        CreateUserResponse response = new CreateUserResponse();
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());

        return response;
    }

    // Metodo pubblico per ottenere un utente dal database tramite il suo ID
    // Restituisce un oggetto CreateUtenteResponse se l'utente esiste, altrimenti lancia un'eccezione
    public CreateUserResponse getUtenteByEmail(String email) throws SQLException {
        Optional<User> utente = userRepository.getUtenteByEmail(email);
        if (utente.isPresent()) {
            CreateUserResponse response = convertToResponse(utente.get());
            return response;
        } else {
            // Gestisci il caso in cui l'utente non esiste
            // Potrebbe essere lanciando un'eccezione o restituendo un valore predefinito
            throw new SQLException("Utente non trovato con email: " + email);
        }
    }

    public User ConvertRequestToUtente(CreateUserRequest cur) {
        User u = new User();
        u.setEmail(cur.getEmail());
        u.setName(cur.getName());
        u.setPswHash(hashCalculator.calculateHash(cur.getPassword()));
        u.setSurname(cur.getSurname());
        return u;
    }

    public List<User> getAllUtenti() {
        //chiamata select
        return userRepository.getUser();
    }

    public void setRuoloOfUser(String email, boolean admin) {
        userRepository.setAdmin(email, admin);
    }

    public boolean getAdmin(String sessionId) {
        try {
            // Get the session by ID and retrieve the associated email
            Session session = sessionRepository.getSessionById(sessionId);
            String email = session.getEmail();

            // Retrieve user information based on the email
            Optional<User> utente = userRepository.getUtenteByEmail(email);

            // Check if the user is an admin
            return utente.isPresent() && utente.get().isAdmin();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
