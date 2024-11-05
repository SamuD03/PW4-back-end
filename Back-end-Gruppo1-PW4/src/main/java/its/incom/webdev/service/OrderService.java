package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Order;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderService {

    public List<Order> getAllOrders() {
        return Order.listAll();
    }
}
