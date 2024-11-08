package its.incom.webdev.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import its.incom.webdev.persistence.model.Ingredient;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngredientRepository implements PanacheRepository<Ingredient> {

    // Find an ingredient by its name
    public Ingredient findByName(String name) {
        return find("name", name).firstResult();
    }
}
