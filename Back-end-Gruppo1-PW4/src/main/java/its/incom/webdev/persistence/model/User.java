package its.incom.webdev.persistence.model;

import jakarta.persistence.*;

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

    // Default no-argument constructor
    public User() {
    }

    // Parameterized constructor
    public User(Integer id, String email, String name, String pswHash, String surname, String number, boolean admin, boolean verified) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.pswHash = pswHash;
        this.surname = surname;
        this.number = number;
        this.admin = admin;
        this.verified = verified;
    }

    // Getters and Setters
    public Integer getId() {
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
        return number; // Ensure this getter is present
    }

    public void setNumber(String number) {
        this.number = number; // Ensure this setter is present
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
}
