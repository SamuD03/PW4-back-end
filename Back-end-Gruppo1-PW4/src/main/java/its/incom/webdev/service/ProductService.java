package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.persistence.repository.ProductRepository;
import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.service.exception.SessionNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class ProductService {
    private final ProductRepository productRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, SessionRepository sessionRepository, UserRepository userRepository){
        this.productRepository = productRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public Product create(String sessionId, String name, String description, Integer quantity, Double price, String category) throws SessionNotFoundException{
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


        try{
            return productRepository.create(name, description, quantity, price, category);
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public Product update(String sessionId, Long productId, String name, String description, Integer quantity, Double price, String category) throws SessionNotFoundException {
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

        try {
            Product product = productRepository.findByProductId(productId);
            if(product == null){
                throw new NotFoundException("Product with" + productId + "not found");
            }

            // Esegui l'aggiornamento solo sui campi non nulli
            if (name != null) product.setName(name);
            if (description != null) product.setDescription(description);
            if (quantity != null) product.setQuantity(quantity); // Ora quantity può essere null
            if (price != null) product.setPrice(price);
            if (category != null) product.setCategory(category);

            return productRepository.edit(product);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void delete(String sessionId, Long productId) throws SessionNotFoundException{
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

        try {
            Product product = productRepository.findByProductId(productId);
            if(product == null){
                throw new NotFoundException("Product with" + productId + "not found");
            }
            productRepository.delete(productId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
