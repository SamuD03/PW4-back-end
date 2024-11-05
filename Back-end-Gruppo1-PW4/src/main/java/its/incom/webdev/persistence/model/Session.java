package its.incom.webdev.persistence.model;

import java.sql.Timestamp;

public class Session {
    private String id;
    private String email;


    // Costruttore senza parametri
    public Session() {
    }

    // Costruttore con parametri
    public Session(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Sessione{" +
                "id=" + id +
                ", email=" + email +
                '}';
    }
}
