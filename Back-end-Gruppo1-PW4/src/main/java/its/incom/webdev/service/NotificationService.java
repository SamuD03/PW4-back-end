package its.incom.webdev.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@ApplicationScoped
public class NotificationService {

    @Inject
    private Mailer mailer;

    @Inject
    private MongoClient mongoClient;

    @Inject
    private javax.sql.DataSource dataSource; // MySQL DataSource

    private static final String REMINDER_SUBJECT = "Reminder: Your Order Pickup is Tomorrow!";

    @Scheduled(every = "1h")
    public void sendPickupReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lessThan24HoursAway = now.plusHours(24);

        // Connect to the MongoDB database and collection
        MongoDatabase database = mongoClient.getDatabase("cest_la_vie"); // Replace with your database name
        MongoCollection<Document> ordersCollection = database.getCollection("orders");

        int emailCount = 0; // Counter for the number of emails sent

        // Retrieve all orders and filter them in Java
        for (Document document : ordersCollection.find()) {
            String pickupStr = document.getString("pickup");
            if (pickupStr == null) {
                continue; // Skip if pickup time is missing
            }

            // Parse the pickup time from the string
            LocalDateTime pickupTime;
            try {
                pickupTime = LocalDateTime.parse(pickupStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                e.printStackTrace(); // Log parsing errors
                continue; // Skip this document if parsing fails
            }

            // Check if the pickup time is within 24 hours from now
            if (pickupTime.isAfter(now) && pickupTime.isBefore(lessThan24HoursAway)) {
                String idBuyer = document.getString("id_buyer");
                String email = getEmailFromMySQL(idBuyer);
                if (email == null) {
                    continue; // Skip if email not found
                }

                // Format the pickup time for the email
                String formattedTime = pickupTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                // Construct the reminder message
                String message = "Remember, you have your order scheduled for pickup tomorrow at " + formattedTime + ".";

                // Send the email
                sendNotificationEmail(email, REMINDER_SUBJECT, message);
                emailCount++; // Increment the counter
            }
        }

        // Log the number of emails sent
        System.out.println("Emails sent: " + emailCount);
    }

    private String getEmailFromMySQL(String idBuyer) {
        String email = null;
        String query = "SELECT email FROM user WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, Integer.parseInt(idBuyer)); // Assuming id_buyer is an integer
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    email = resultSet.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead of printing stack trace
        }

        return email;
    }

    public void sendNotificationEmail(String to, String subject, String message) {
        String htmlMessage = "<html><body><p>" + message + "</p></body></html>";

        Mail email = Mail.withText(to, subject, message)
                .setHtml(htmlMessage);
        mailer.send(email);

        System.out.println("Notification email sent to: " + to);
    }
}