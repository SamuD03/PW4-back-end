package its.incom.webdev.persistence.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderRepository {

    private final MongoCollection<Document> ordersCollection;

    @Inject
    public OrderRepository(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("cest_la_vie");
        this.ordersCollection = database.getCollection("order");
    }

    @Inject
    DataSource dataSource;

    public boolean bookOrder(String emailBuyer, List<Map<String, Object>> content, LocalDateTime pickupTime, String comment) {
        try {
            // check if the pickup time slot is already booked
            if (isTimeSlotBooked(pickupTime)) {
                System.out.println("Time slot is already booked: " + pickupTime);
                return false; // Time not avaliab
            }

            // retrieve product details from MySQL
            List<Document> productDetails = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                for (Map<String, Object> item : content) {
                    String productName = (String) item.get("name");
                    int quantity = (int) item.get("quantity");

                    String query = "SELECT id, productName, description, price, category FROM product WHERE productName = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, productName);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (resultSet.next()) {
                                // create and add
                                Document product = new Document("id", resultSet.getInt("id"))
                                        .append("name", resultSet.getString("productName"))
                                        .append("description", resultSet.getString("description"))
                                        .append("price", resultSet.getBigDecimal("price"))
                                        .append("category", resultSet.getString("category"))
                                        .append("quantity", quantity);
                                productDetails.add(product);
                            }
                        }
                    }
                }
            }

            // create the order document
            Document order = new Document("email_buyer", emailBuyer)
                    .append("content", productDetails)
                    .append("pickup", pickupTime.toString())
                    .append("comment", comment)
                    .append("created_at", LocalDateTime.now().toString());

            // debug
            System.out.println("Attempting to insert order into MongoDB: " + order.toJson());

            // Insert
            ordersCollection.insertOne(order);

            // debug
            System.out.println("Order successfully inserted into MongoDB.");
            return true; // Order successfully created
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating order", e);
        }
    }

    // check if alr booked
    private boolean isTimeSlotBooked(LocalDateTime pickupTime) {
        LocalDateTime endTime = pickupTime.plusMinutes(9);
        Document existingOrder = ordersCollection.find(
                new Document("$or", List.of(
                        new Document("pickup", new Document("$lt", endTime.toString()).append("$gte", pickupTime.toString())),
                        new Document("pickup", new Document("$lt", endTime.toString()).append("$gte", pickupTime.minusMinutes(9).toString()))
                ))
        ).first();
        return existingOrder != null;
    }
}
