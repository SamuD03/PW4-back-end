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

import static io.quarkus.mongodb.panache.PanacheMongoEntityBase.persist;

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

        String query = "INSERT INTO user (name, surname, email, pswHash, admin, emailVerified) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setBoolean(5,user.isAdmin());
            statement.setBoolean(6,user.isEmailVerified());

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
            // Log the exception (use a logging framework or print the stack trace)
            e.printStackTrace();
            throw new RuntimeException("Errore durante il controllo dell'utente", e);
        }
    }

    //controllare se non servono le altre info dell'utente
    public Optional<User> findByEmailPsw(String email, String pswHash) {
        try {
            try (Connection connection = database.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT email,pswHash FROM user WHERE email = ? AND pswHash = ?")) {
                    statement.setString(1, email);
                    statement.setString(2, pswHash);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var utente = new User();
                        utente.setEmail(resultSet.getString("email"));
                        utente.setPswHash(resultSet.getString("pswHash"));
                        return Optional.of(utente);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        try {
            try (Connection connection = database.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT email, pswHash, emailVerified FROM user WHERE email = ?")) {
                    statement.setString(1, email);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var user = new User();
                        user.setEmail(resultSet.getString("email"));
                        user.setPswHash(resultSet.getString("pswHash"));
                        user.setEmailVerified(resultSet.getBoolean("emailVerified"));
                        return Optional.of(user);
                    }
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
                    user.setName(resultSet.getString("nome"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPswHash(resultSet.getString("pswHash"));
                    boolean isAdmin = resultSet.getBoolean("admin");

                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            // Log the exception (use a logging framework or print the stack trace)
            e.printStackTrace();
            throw new RuntimeException("Errore durante la ricerca dell'utente", e);
        }
    }
    public List<User> getUser(){
        List<User> list=new ArrayList<>();
        String query = "SELECT name,surname,email,admin FROM user";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet resultSet = statement.executeQuery()) {
                while(resultSet.next()) {
                    User user = new User();
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setEmail(resultSet.getString("email"));
                    //prendo l'esito in stringa la formatto per ENUM di Java
                    boolean isAdmin = resultSet.getBoolean("admin");
                    list.add(user);
                }return list;
            }
        } catch (SQLException e) {
            // Log the exception (use a logging framework or print the stack trace)
            e.printStackTrace();
            throw new RuntimeException("Errore durante la selezione degli utenti", e);
        }
    }
  /*  public void setAdmin(String email,boolean admin){
        String query = "UPDATE user SET admin = ? WHERE email = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, admin);
            statement.setString(2, email);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                //eccezione personalizzata mancante
                throw new RuntimeException("Nessun utente trovato con l'ID specificato");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento dell'utente", e);
        }
    }*/


    public void updateEmailVerified(String email, boolean emailVerified) {
        String query = "UPDATE user SET emailVerified = ? WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, emailVerified);
            statement.setString(2, email);

            int rowsAffected = statement.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //controllo admin
    public boolean checkAdmin(String email) throws SQLException {
        String query = "SELECT admin FROM user WHERE email = ?";

        try (Connection connection = database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean("admin");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nel controllo del ruolo admin", e);
        }

        return false;
    }

    public User create(User user) {
        String query = "INSERT INTO user (name, surname, email, pswHash, admin, emailVerified) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getSurname());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPswHash());
            statement.setBoolean(5, user.isAdmin());
            statement.setBoolean(6, user.isEmailVerified());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la creazione dell'utente", e);
        }

        return user;
    }

}
