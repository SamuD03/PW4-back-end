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
            // Check if a product with the same name already exists
            if (productRepository.existsByName(name)) {
                throw new IllegalArgumentException("Product with name '" + name + "' already exists");
            }

            // Create a new product
            Product product = new Product(name, description, quantity, price, category);

            // Associate ingredients with the product
            if (ingredientNames != null && !ingredientNames.isEmpty()) {
                Set<Ingredient> ingredients = new HashSet<>();  // Set to avoid duplicates

                for (String ingredientName : ingredientNames) {
                    // Find the ingredient by name
                    Ingredient ingredient = ingredientRepository.findByName(ingredientName);
                    if (ingredient == null) {
                        // Create the ingredient if it doesn't exist
                        ingredient = new Ingredient();
                        ingredient.setName(ingredientName);
                        ingredientRepository.persist(ingredient);
                    }

                    // Add the ingredient to the set of ingredients for the product
                    ingredients.add(ingredient);
                }

                // Set the product's ingredients
                product.setIngredients(ingredients);  // This automatically links the ingredients to the product
            }

            // persist the product with its ingredients
            productRepository.persist(product);  // No need for manual persistence of product-ingredient relation

            return product;
        } catch (PersistenceException e) {
            throw new RuntimeException("Error creating product: " + e.getMessage());
        }
    }


    @Transactional
    public Product update(String sessionId, Long productId, String productName, String description, Integer quantity, Double price, String category, Set<String> ingredientNames, String url) throws SessionNotFoundException {
        try {
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

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " not found"));

        if (productName != null) product.setProductName(productName);
        if (description != null) product.setDescription(description);
        if (quantity != null) product.setQuantity(quantity);
        if (price != null) product.setPrice(price);
        if (category != null) product.setCategory(category);
        if (url != null) product.setUrl(url);

        if (ingredientNames != null && !ingredientNames.isEmpty()) {
            Set<Ingredient> ingredients = new HashSet<>(); // Set to avoid duplicates

            for (String ingredientName : ingredientNames) {
                // Find the ingredient by name
                Ingredient ingredient = ingredientRepository.findByName(ingredientName);
                if (ingredient == null) {
                    // Create the ingredient if it doesn't exist
                    ingredient = new Ingredient();
                    ingredient.setName(ingredientName);
                    ingredientRepository.persist(ingredient);
                }

                // Add the ingredient to the set
                ingredients.add(ingredient);
            }

            // Set the new ingredients for the product
            product.setIngredients(ingredients);
        }

        // Persist the updated product
        try {
            productRepository.persist(product);
        } catch (PersistenceException e) {
            throw new RuntimeException("Error updating product: " + e.getMessage());
        }

        return product;
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

    public List<Product> getAll(String sessionId){
        try{
            return productRepository.getAll();
        } catch (PersistenceException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void updateProductQuantity(Integer id, Integer quantity) {
        try {
            // fetch the product by ID
            Product product = productRepository.findByProductId(Long.valueOf(id))
                    .orElseThrow(() -> new NotFoundException("Product with ID " + id + " not found"));

            // update the product quantity
            product.setQuantity(product.getQuantity() + quantity);

            // persist the updated product with an empty set of ingredient names
            productRepository.edit(product, new HashSet<>());
        } catch (PersistenceException e) {
            throw new RuntimeException("Failed to update product quantity: " + e.getMessage(), e);
        }
    }
    @Transactional
    public void uploadImage(String sessionId, Long productId, String imageUrl) throws SessionNotFoundException {
        try {
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


        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " not found"));

        product.setUrl(imageUrl);
        productRepository.edit(product, new HashSet<>());
    }
}
