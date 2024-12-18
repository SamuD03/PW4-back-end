package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Ingredient;
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
import java.util.List;

@ApplicationScoped
public class IngredientService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public IngredientService(SessionRepository sessionRepository, UserRepository userRepository, ProductRepository productRepository){
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Inject
    IngredientRepository ingredientRepository;

    @Transactional
    public Ingredient create(String sessionId, String ingredientName) throws SessionNotFoundException{
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

        if(ingredientRepository.findByName(ingredientName) != null){
            throw new RuntimeException("Ingredient already exists");
        }

        // Validate the new ingredient name
        if (ingredientName == null || ingredientName.isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }

        try{
            Ingredient ingredient = new Ingredient();
            ingredient.setName(ingredientName);
            ingredientRepository.create(ingredient);
            return ingredient;
        } catch (PersistenceException e){
            throw new PersistenceException(e.getMessage());
        }
    }

    // Method to update an existing ingredient
    @Transactional
    public Ingredient update(String sessionId, Integer ingredientId, String newIngredientName) throws SessionNotFoundException {
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

        // Validate the new ingredient name
        if (newIngredientName == null || newIngredientName.isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }

        // Find the ingredient to update
        Ingredient ingredient = ingredientRepository.findById(Long.valueOf(ingredientId));
        if (ingredient == null) {
            throw new RuntimeException("Ingredient not found with ID: " + ingredientId);
        }


        // Check if another ingredient with the new name exists (optional check for uniqueness)
        if (ingredientRepository.findByName(newIngredientName) != null &&
                !ingredient.getName().equals(newIngredientName)) {
            throw new RuntimeException("Ingredient with name '" + newIngredientName + "' already exists.");
        }

        // Update the ingredient's name
        ingredient.setName(newIngredientName);

        // Persist the updated ingredient
        ingredientRepository.update(ingredient);

        return ingredient;
    }

    public List<Ingredient> getAll(String sessionId) throws SessionNotFoundException{
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

        try{
            return ingredientRepository.listAll();
        } catch (PersistenceException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void delete(String sessionId, Long ingredientId) throws SessionNotFoundException {
        try {
            // validate session and check admin privileges
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

        // find the ingredient to delete
        Ingredient ingredient = ingredientRepository.findById(ingredientId);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient not found with ID: " + ingredientId);
        }

        // check if the ingredient is being used by any product
        boolean isUsed = productRepository.isIngredientUsed(ingredientId);
        if (isUsed) {
            throw new RuntimeException("Ingredient is being used by a product");
        }

        // delete the ingredient
        ingredientRepository.delete(ingredient);
    }
}
