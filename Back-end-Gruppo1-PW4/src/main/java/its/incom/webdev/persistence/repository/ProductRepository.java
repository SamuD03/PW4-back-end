package its.incom.webdev.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import its.incom.webdev.persistence.model.Ingredient;
import its.incom.webdev.persistence.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    @Inject
    IngredientRepository ingredientRepository;

    public Product edit(Product product, Set<String> ingredientNames) {
        // Update the product details
        product.persist(); // Aggiorna il prodotto

        // Update ingredients if provided
        if (ingredientNames != null && !ingredientNames.isEmpty()) {
            Set<Ingredient> ingredients = new HashSet<>();

            for (String ingredientName : ingredientNames) {
                Ingredient ingredient = ingredientRepository.findByName(ingredientName);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }

            // Set the product's ingredients
            product.setIngredients(ingredients);
        }

        return product;
    }

    public Optional<Product> findByProductId(Long id) {
        return find("SELECT p FROM Product p LEFT JOIN FETCH p.ingredients WHERE p.id = ?1", id).firstResultOptional(); // Metodo Panache per trovare un prodotto per ID
    }
    public void deleteProduct(Long productId) {
        // Step 1: Remove the associations in the junction table (product_ingredient)
        getEntityManager()
                .createNativeQuery("DELETE FROM product_ingredient WHERE product_id = :productId")
                .setParameter("productId", productId)
                .executeUpdate();

        // Step 2: Delete the product itself
        deleteById(productId);
    }

    public List<Product> getAll() {
        List<Product> products = find("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.ingredients").list();
        System.out.println(products);
        return products;
    }

    public boolean existsByName(String name){
        return find("productName", name).firstResult() != null;
    }

    public boolean isIngredientUsed(Long ingredientId) {
        return find("SELECT 1 FROM Product p JOIN p.ingredients i WHERE i.id = ?1", ingredientId)
                .firstResultOptional()
                .isPresent();
    }
}
