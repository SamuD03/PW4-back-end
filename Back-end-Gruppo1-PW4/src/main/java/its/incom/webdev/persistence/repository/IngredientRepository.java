package its.incom.webdev.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import its.incom.webdev.persistence.model.Ingredient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class IngredientRepository implements PanacheRepository<Ingredient> {

    // Find an ingredient by its name
    public Ingredient findByName(String name) {
        return find("name", name).firstResult();
    }
    public void create(Ingredient ingredient) {
        persist(ingredient);
    }
    public void update(Ingredient ingredient) {
        if (ingredient != null && ingredient.getId() != null) {
            // This will update the ingredient with the given ID
            persist(ingredient);
        } else {
            throw new IllegalArgumentException("Ingredient must have a valid ID to be updated.");
        }
    }
}
