package its.incom.webdev.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Ingredient extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Getters and Setters for id and name
    public Integer getId() {
        return id;  // Return the id value
    }

    public void setId(Integer id) {
        this.id = id;  // Set the id value
    }

    public String getName() {
        return name;  // Return the name of the ingredient
    }

    public void setName(String name) {
        this.name = name;  // Set the name of the ingredient
    }

    // Optional: Override toString() for easier debugging or logging
    @Override
    public String toString() {
        return "Ingredient{id=" + id + ", name='" + name + "'}";
    }
}
