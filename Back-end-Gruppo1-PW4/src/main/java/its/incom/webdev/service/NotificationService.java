package its.incom.webdev.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import its.incom.webdev.persistence.model.Order;
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

    public void logOrderOwner(String orderId) {
        // Find the order in MongoDB
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            // Extract the buyer ID from the order
            int buyerId = order.getBuyerId(); // Adjust this getter if needed

            // Find the user in MySQL using the buyer ID
            Optional<User> optionalUser = userRepository.findById(buyerId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // Log user details to the console
                System.out.println("Order Owner Details:");
                System.out.println("ID: " + user.getId());
                System.out.println("Name: " + user.getName());
                System.out.println("Surname: " + user.getSurname());
                System.out.println("Email: " + user.getEmail());
            } else {
                System.out.println("User not found in MySQL with ID: " + buyerId);
            }
        } else {
            System.out.println("Order not found in MongoDB with ID: " + orderId);
        }
    }
//    @Scheduled(every = "10s")
//    public void testLogOrderOwner() {
//        String mockOrderId = "672f66075e37015b30f23d08";
//        logOrderOwner(mockOrderId);
//    }
//    @Scheduled(every = "10s")
//    public void printAllPickupTimes() {
//        // Retrieve all orders from the MongoDB repository
//        Iterable<Order> allOrders = orderRepository.findAllOrders();
//
//        // Loop through each order and print the pickup time
//        System.out.println("Printing Pickup Times for All Orders:");
//        for (Order order : allOrders) {
//            System.out.println("Order ID: " + order.id + ", Pickup Time: " + order.getPickupDate());
//        }
//    }
@Scheduled(every = "24h")
public void printPickupTimesWithin24Hours() {
    // now
    LocalDateTime now = LocalDateTime.now();
    // +24
    LocalDateTime timeLimit = now.plusHours(24);

    // retrieve all orders from the repository
    Iterable<Order> allOrders = orderRepository.findAllOrders();
    boolean foundOrders = false;

    System.out.println("Orders with Pickup Time Within the Next 24 Hours:");

    // loop through each order and check if the pickup time is within the next 24 hours
    for (Order order : allOrders) {
        if (order.getDateTime() != null && order.getDateTime().isAfter(now) && order.getDateTime().isBefore(timeLimit)) {
            // retrieve the user from the MySQL repository using the buyer ID
            Optional<User> optionalUser = userRepository.findById(Integer.parseInt(order.getIdBuyer()));

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // print the order details and user email
                System.out.println("Order ID: " + order.getId() + ", Pickup Time: " + order.getPickupDate());
                System.out.println("User Email: " + user.getEmail());
            } else {
                // ff the user is not found, print an error message
                System.out.println("Order ID: " + order.getId() + ", Pickup Time: " + order.getPickupDate());
                System.out.println("User Email: Not found");
            }

            foundOrders = true;
        }
    }

    // ff no orders are found, print a message
    if (!foundOrders) {
        System.out.println("No orders found within the next 24 hours.");
    }
}
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
                // extract the buyer ID from the order
                int buyerId = order.getBuyerId(); // Adjust this getter if needed

                // find the user in MySQL using the buyer ID
                Optional<User> optionalUser = userRepository.findById(buyerId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    String userName = user.getName() + " " + user.getSurname();

                    // call the method to send the email
                    sendPickupReminderEmail(user.getEmail(), order.getId().toString(), order.getDateTime(), userName);
                } else {
                    System.out.println("User not found in MySQL with ID: " + buyerId);
                }
            }
        }
    }
    public void sendPickupReminderEmail(String to, String orderId, LocalDateTime pickupTime, String userName) {
        // use the user's full name provided as an argument
        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }

        // format the pickup time to a readable string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedPickupTime = pickupTime.format(formatter);

        // create the email content
        String subject = "Reminder: Your Pickup Time is Almost Here!";
        String textBody = "Hello " + userName + ",\n\nThis is a reminder that your pickup time is scheduled for: "
                + formattedPickupTime + ".\nPlease be prepared to collect your order.\n\nThank you!";
        String htmlBody = "<html><body><p>Hello " + userName + ",</p><p>This is a reminder that your pickup time is scheduled for:</p>"
                + "<p><strong>" + formattedPickupTime + "</strong></p>"
                + "<p>Please be prepared to collect your order.</p><p>Thank you!</p></body></html>";

        // log the email details
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Text Body: " + textBody);

        try {
            // create and send the email
            Mail email = Mail.withText(to, subject, textBody)
                    .setHtml(htmlBody);
            mailer.send(email);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + to);
            e.printStackTrace();
        }
    }
}