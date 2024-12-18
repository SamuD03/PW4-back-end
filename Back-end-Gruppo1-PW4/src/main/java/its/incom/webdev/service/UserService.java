package its.incom.webdev.service;


import its.incom.webdev.persistence.model.*;

import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.rest.model.CreateUserResponse;
import its.incom.webdev.service.exception.SessionNotFoundException;
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

    public List<CreateUserResponse> getUsers(String sessionId, boolean admin)throws SessionNotFoundException {
        try{
            //controllo sessione
            Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
            if (userId == null){
                throw new SessionNotFoundException("Please log in");
            }

            //controllo admin
            if(!userRepository.checkAdmin(userId)){
                throw new SecurityException("Access denied");
            }
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }

        try {
            return userRepository.getFilteredUsers(admin);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public boolean getAdmin(String sessionId) {
        try {
            // Get the session by ID and retrieve the associated email
            Session session = sessionRepository.getSessionById(sessionId);

            // retrive user based on info
            Optional<User> utente =   userRepository.findByEmailOrNumber(session.getUser().getEmail(), session.getUser().getNumber());


            // is user admin?
            return utente.isPresent() && utente.get().isAdmin();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateNotificationPreference(String sessionId, boolean notification) throws Exception {
        Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
        if (userId == null) {
            throw new Exception("Invalid session ID");
        }

        return userRepository.updateNotificationPreference(userId, notification);
    }

    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    public boolean deleteUserById(int userIdToDelete) {
        return userRepository.deleteUserById(userIdToDelete);
    }

    public boolean updateUser(User user) {
        return userRepository.updateUser(user);
    }

    public Optional<User> findByEmailOrNumber(String email, String phoneNumber) {
        return userRepository.findByEmailOrNumber(email, phoneNumber);
    }

    public void updateVerifiedWithPhone(String phoneNumber, boolean verified) {
        userRepository.updateVerifiedWithPhone(phoneNumber, verified);
    }

    public Optional<User> findNumber(String phoneNumber) {
        return userRepository.findNumber(phoneNumber);
    }

    public User create(User u) {
        return userRepository.create(u);
    }

    public void updateVerified(String email, boolean verified) {
        userRepository.updateVerified(email, verified);
    }
}
