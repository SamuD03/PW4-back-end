package its.incom.webdev.persistence.model;

public class User {
    private String email;
    private String name;
    private String pswHash;
    private String surname;
    private boolean admin;
    private boolean emailVerified;

    public User(String email, String name, String cognome, String pswHash, String surname, boolean admin, boolean emailVerified) {
        this.email = email;
        this.name = name;
        this.pswHash = pswHash;
        this.surname = surname;
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
