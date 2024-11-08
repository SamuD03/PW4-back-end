package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Order;
import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.OrderRepository;
import its.incom.webdev.persistence.repository.ProductRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private UserRepository userRepository;

    public List<Order> getAllOrders() {
        return Order.listAll();
    }

    // Method to book an order
    public boolean bookOrder(Integer userId, String pswHash, List<Map<String, Object>> content, LocalDateTime pickupTime, String comment) {
        try {
            // verify the user exists and the password is correct
            Optional<User> userOptional = userRepository.findByIdAndPsw(userId, pswHash);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found or password incorrect for ID: " + userId);
            }

            // loop through the content to decrease product quantities
            for (Map<String, Object> item : content) {
                Integer productId = (Integer) item.get("productId");
                Integer quantity = (Integer) item.get("quantity");

                // retrieve the product from the database
                Product product = productRepository.findById(Long.valueOf(productId));
                if (product == null) {
                    throw new IllegalArgumentException("Product with ID " + productId + " not found");
                }

                // decrease the product quantity
                product.decreaseQuantity(quantity);

                // update the product in the database
                productRepository.persist(product);
            }

            // delegate booking logic to repository to save the order
            return orderRepository.bookOrder(String.valueOf(userId), content, pickupTime, comment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating order", e);
        }
    }
}
