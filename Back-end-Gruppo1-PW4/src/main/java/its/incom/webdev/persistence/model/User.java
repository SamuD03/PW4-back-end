package its.incom.webdev.persistence.model;

import jakarta.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pswHash", nullable = false)
    private String pswHash;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "number", unique = true)
    private String number;

    @Column(name = "admin", nullable = false)
    private boolean admin;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "notification", nullable = false)
    private boolean notification;

    // Default no-argument constructor
    public User() {
    }

    // Parameterized constructor
    public User(Integer id, String email, String name, String pswHash, String surname, String number, boolean admin, boolean verified, boolean notification) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.pswHash = pswHash;
        this.surname = surname;
        this.number = number;
        this.admin = admin;
        this.verified = verified;
        this.notification = notification;
    }

    // Constructor to initialize from ResultSet
    public User(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.email = resultSet.getString("email");
        this.name = resultSet.getString("name");
        this.pswHash = resultSet.getString("pswHash");
        this.surname = resultSet.getString("surname");
        this.number = resultSet.getString("number");
        this.admin = resultSet.getBoolean("admin");
        this.verified = resultSet.getBoolean("verified");
        this.notification = resultSet.getBoolean("notification"); // New field
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPswHash() {
        return pswHash;
    }

    public void setPswHash(String pswHash) {
        this.pswHash = pswHash;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }
}
