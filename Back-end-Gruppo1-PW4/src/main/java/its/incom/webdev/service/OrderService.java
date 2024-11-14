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
        return orderRepository.findAllOrders();
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

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findOrdersByUserId(String.valueOf(userId));
    }

    public boolean updateOrderStatus(String orderId, String newStatus, boolean isAdmin, String userId) throws IllegalAccessException {
        try {
            // fetch the order from the repository
            Order order = orderRepository.findOrderById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found");
            }

            // check if the user is an admin
            if (isAdmin) {
                // admins can update to any of the valid statuses
                List<String> validStatuses = List.of("delivered", "cancelled", "pending", "confirmed");
                if (!validStatuses.contains(newStatus)) {
                    throw new IllegalArgumentException("Invalid status");
                }
            } else {
                // non-admin users can only cancel pending orders
                if (!order.getIdBuyer().equals(userId)) {
                    throw new IllegalAccessException("User not authorized to update this order");
                }
                if (!order.getStatus().equals("pending")) {
                    throw new IllegalArgumentException("Only pending orders can be cancelled");
                }
                if (!newStatus.equals("cancelled")) {
                    throw new IllegalArgumentException("User can only change the status to 'cancelled'");
                }
            }

            // update the order status in the repository
            boolean updated = orderRepository.updateOrderStatus(orderId, newStatus);
            if (!updated) {
                throw new RuntimeException("Failed to update order status in the repository");
            }

            return true; // status updated successfully
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage()); // "User not authorized to update this order"
            throw e; // re-throw to be caught by OrderResource
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Specific message for argument issues
            throw e; // re-throw to be caught by OrderResource
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order getOrderById(String orderId) {
        try {
            // fetch the order from the repository using the order ID
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                return orderOptional.get();
            } else {
                throw new IllegalArgumentException("Order not found for ID: " + orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching order by ID", e);
        }
    }
}


