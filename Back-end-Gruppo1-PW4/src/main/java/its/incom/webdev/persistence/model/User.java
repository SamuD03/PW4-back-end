package its.incom.webdev.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "pswHash")
    private String pswHash;

    @Column(name = "surname")
    private String surname;

    @Column(name = "number")
    private String number;

    @Column(name = "admin")
    private boolean admin;

    @Column(name = "emailVerified")
    private boolean emailVerified;

    public User(String email, String name, String pswHash, String surname, String number, boolean admin, boolean emailVerified) {
        this.email = email;
        this.name = name;
        this.pswHash = pswHash;
        this.surname = surname;
        this.number = number;
        this.admin = admin;
        this.emailVerified = emailVerified;
    }

    public User() {
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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
