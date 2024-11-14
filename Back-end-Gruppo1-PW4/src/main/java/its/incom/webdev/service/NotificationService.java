package its.incom.webdev.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import its.incom.webdev.persistence.model.Order;
import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.persistence.model.User;
import its.incom.webdev.persistence.repository.OrderRepository;
import its.incom.webdev.persistence.repository.UserRepository;

import com.mongodb.client.MongoClient;
import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificationService {

    @Inject
    Mailer mailer;
    
    @Inject
    OrderRepository orderRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    private MongoClient mongoClient;

    @Inject
    private DataSource dataSource;


    @Scheduled(every = "24h")
    public void checkAndSendPickupReminders() {
        // get the current time and the 24-hour time limit
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeLimit = now.plusHours(24);

        // retrieve all orders
        Iterable<Order> allOrders = orderRepository.findAllOrders();

        // loop through orders and find those with a pickup time within the next 24 hours
        for (Order order : allOrders) {
            if (order.getDateTime() != null && order.getDateTime().isAfter(now) && order.getDateTime().isBefore(timeLimit)) {
                // check if the order status is not "cancelled"
                if (!"cancelled".equalsIgnoreCase(order.getStatus())) {
                    // extract the buyer ID from the order
                    int buyerId = order.getBuyerId(); // Adjust this getter if needed

                    // find the user in MySQL using the buyer ID
                    Optional<User> optionalUser = userRepository.findById(buyerId);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();

                        // check if the user has notifications enabled
                        if (user.isNotification()) {
                            String userName = user.getName() + " " + user.getSurname();

                            // call the method to send the email
                            sendPickupReminderEmail(user.getEmail(), order.getId().toString(), order.getDateTime(), userName);
                        } else {
                            System.out.println("User with ID: " + buyerId + " has notifications disabled.");
                        }
                    } else {
                        System.out.println("User not found in MySQL with ID: " + buyerId);
                    }
                }
            }
        }
    }

    public void sendPickupReminderEmail(String to, String orderId, LocalDateTime pickupTime, String userName) {
        // Use the user's full name provided as an argument
        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }

        // Format the pickup time to a more readable string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd 'at' HH:mm");
        String formattedPickupTime = pickupTime.format(formatter);

        // Create the email content
        String subject = "Reminder: Your Pickup Time is Almost Here!";
        String textBody = "Hello " + userName + ",\n\nThis is a reminder that your pickup time is scheduled for: "
                + formattedPickupTime + ".\nPlease be prepared to collect your order.\n\nThank you!";
        String htmlBody = "<html><body><p>Hello " + userName + ",</p><p>This is a reminder that your pickup time is scheduled for:</p>"
                + "<p><strong>" + formattedPickupTime + "</strong></p>"
                + "<p>Please be prepared to collect your order.</p><p>Thank you!</p></body></html>";

        // Log the email details
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Text Body: " + textBody);

        try {
            // Create and send the email
            Mail email = Mail.withText(to, subject, textBody)
                    .setHtml(htmlBody);
            mailer.send(email);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + to);
            e.printStackTrace();
        }
    }

    public void notifyAdminsAboutNewOrder(Order order, String buyerName) {
        // retrieve all admins from the database
        List<User> adminUsers = userRepository.getAllAdmins();

        // format the order details
        StringBuilder orderDetailsBuilder = new StringBuilder();
        orderDetailsBuilder.append("Order ID: ").append(order.getId().toHexString()).append("\n");
        orderDetailsBuilder.append("Buyer ID: ").append(order.getIdBuyer()).append("\n");
        orderDetailsBuilder.append("Pickup Time: ").append(order.getPickupDate()).append("\n");
        orderDetailsBuilder.append("Comment: ").append(order.getComment()).append("\n");
        orderDetailsBuilder.append("Status: ").append(order.getStatus()).append("\n");
        orderDetailsBuilder.append("Items Ordered:\n");

        // add each product's details
        for (Product item : order.getContent()) {
            orderDetailsBuilder.append("- ").append(item.getProductName())
                    .append(" (Quantity: ").append(item.getQuantity()).append(")\n")
                    .append("  Description: ").append(item.getDescription()).append("\n")
                    .append("  Price: $").append(String.format("%.2f", item.getPrice())).append("\n")
                    .append("  Category: ").append(item.getCategory()).append("\n\n");
        }

        String orderDetails = orderDetailsBuilder.toString();

        // send the email to all admins
        for (User admin : adminUsers) {
            // compose email content
            String subject = "New Order Arrived!";
            String body = "Hello Admin,\n\nA new order has been placed by " + buyerName + ".\n\nOrder Details:\n" + orderDetails + "\nPlease review the order.";

            // send email
            mailer.send(Mail.withText(admin.getEmail(), subject, body));
        }
    }

}