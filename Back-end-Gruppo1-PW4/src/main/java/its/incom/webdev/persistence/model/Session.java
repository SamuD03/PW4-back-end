package its.incom.webdev.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Session extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // Session ID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Reference to the User entity

    // Default constructor
    public Session() {
    }

    // Constructor with parameters
    public Session(String id, User user) {
        this.id = id;
        this.user = user;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", user=" + user +
                '}';
    }

}
