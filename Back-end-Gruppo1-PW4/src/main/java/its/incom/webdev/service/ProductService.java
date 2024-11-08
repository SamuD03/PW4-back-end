package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Ingredient;
import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.persistence.repository.IngredientRepository;
import its.incom.webdev.persistence.repository.ProductRepository;
import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.service.exception.SessionNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ProductService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    IngredientRepository ingredientRepository;

    public ProductService(SessionRepository sessionRepository, UserRepository userRepository){
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Product create(
            String sessionId,
            String name,
            String description,
            Integer quantity,
            Double price,
            String category,
            Set<String> ingredientNames
    ) throws SessionNotFoundException {
        try {
            // Validate session and check admin privileges
            Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
            if (userId == null) {
                throw new SessionNotFoundException("Please log in");
            }
            if (!userRepository.checkAdmin(userId)) {
                throw new SecurityException("Access denied");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error validating session: " + e.getMessage());
        }

        try {
            // Create a new product
            Product product = new Product(name, description, quantity, price, category);

            // Associate ingredients with the product
            if (ingredientNames != null && !ingredientNames.isEmpty()) {
                Set<Ingredient> ingredients = new HashSet<>();  // Set to avoid duplicates

                for (String ingredientName : ingredientNames) {
                    // Find the ingredient by name
                    Ingredient ingredient = ingredientRepository.findByName(ingredientName);
                    if (ingredient == null) {
                        throw new RuntimeException("Ingredient not found: " + ingredientName);
                    }

                    // Add the ingredient to the set of ingredients for the product
                    ingredients.add(ingredient);
                }

                // Set the product's ingredients
                product.setIngredients(ingredients);  // This automatically links the ingredients to the product

                // Persist the product with its ingredients
                productRepository.persist(product);  // No need for manual persistence of product-ingredient relation
            }

            return product;
        } catch (PersistenceException e) {
            throw new RuntimeException("Error creating product: " + e.getMessage());
        }
    }


    @Transactional
    public Product update(String sessionId, Long productId, String productName, String description, Integer quantity, Double price, String category, Set<String> ingredientNames) throws SessionNotFoundException {
        try{
            //controllo sessione
            Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
            if (userId == null){
                throw new SessionNotFoundException("Please log in");
            }

            //controllo admin
            if(!userRepository.checkAdmin(userId)){
                throw new SecurityException("Access denied");
            }
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Product with " + productId + " not found"));

        // Update product fields if new values are provided
        if (productName != null) product.setProductName(productName);
        if (description != null) product.setDescription(description);
        if (quantity != null) product.setQuantity(quantity);
        if (price != null) product.setPrice(price);
        if (category != null) product.setCategory(category);

        // Update the ingredients
        if (ingredientNames != null && !ingredientNames.isEmpty()) {
            Set<Ingredient> ingredients = new HashSet<>();

            for (String ingredientName : ingredientNames) {
                Ingredient ingredient = ingredientRepository.findByName(ingredientName);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }

            // Set the new ingredients for the product
            product.setIngredients(ingredients);
        }

        // Persist the updated product (the edit method is already updated to handle ingredients)
        return productRepository.edit(product, ingredientNames);
    }

    @Transactional
    public void delete(String sessionId, Long productId) throws SessionNotFoundException {
        try {
            // Session check
            Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
            if (userId == null) {
                throw new SessionNotFoundException("Please log in");
            }

            // Admin check
            if (!userRepository.checkAdmin(userId)) {
                throw new SecurityException("Access denied");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Find the product to delete
        productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Product with " + productId + " not found"));

        // Delete the product and its associations with ingredients
        productRepository.deleteProduct(productId);
    }
}
