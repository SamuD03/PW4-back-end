package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Order;
import its.incom.webdev.persistence.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return Order.listAll(); // Ensure Order extends PanacheMongoEntity
    }

    // book order
    public boolean bookOrder(String emailBuyer, List<Map<String, Object>> content, LocalDateTime pickupTime, String comment) {
        try {
            // Delegate booking logic to repository
            return orderRepository.bookOrder(emailBuyer, content, pickupTime, comment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating order", e);
        }
    }
}