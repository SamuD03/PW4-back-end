package its.incom.webdev.persistence.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import its.incom.webdev.persistence.model.Order;
import its.incom.webdev.persistence.model.Product;
import org.bson.Document;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    public boolean bookOrder(String IdBuyer, List<Map<String, Object>> content, LocalDateTime pickupTime, String comment) {
        try {
            // Check if the pickup time slot is already booked
            if (isTimeSlotBooked(pickupTime)) {
                System.out.println("Time slot is already booked: " + pickupTime);
                return false; // Time not available
            }

            // Retrieve product details from MySQL and decrease the quantity
            List<Document> productDetails = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                for (Map<String, Object> item : content) {
                    // Safely cast and validate the content data
                    Object productIdObj = item.get("productId");
                    if (!(productIdObj instanceof Integer)) {
                        throw new IllegalArgumentException("Product ID must be an integer");
                    }
                    int productId = (int) productIdObj;

                    Object quantityObj = item.get("quantity");
                    if (!(quantityObj instanceof Integer)) {
                        throw new IllegalArgumentException("Quantity must be an integer");
                    }
                    int quantity = (int) quantityObj;

                    // Query to fetch product details using productId
                    String selectQuery = "SELECT id, productName, description, price, category, quantity FROM product WHERE id = ?";
                    try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                        selectStatement.setInt(1, productId);
                        try (ResultSet resultSet = selectStatement.executeQuery()) {
                            if (resultSet.next()) {
                                // Get the current quantity
                                int currentQuantity = resultSet.getInt("quantity");

                                // Check and decrease the quantity
                                if (currentQuantity < quantity) {
                                    throw new IllegalArgumentException("Insufficient quantity available for product ID: " + productId);
                                }

                                // Decrease the quantity
                                int newQuantity = currentQuantity - quantity;

                                // Update the product quantity in the database
                                String updateQuery = "UPDATE product SET quantity = ? WHERE id = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setInt(1, newQuantity);
                                    updateStatement.setInt(2, productId);
                                    updateStatement.executeUpdate();
                                }

                                // Create and add product details to the list
                                Document product = new Document("id", resultSet.getInt("id"))
                                        .append("name", resultSet.getString("productName"))
                                        .append("description", resultSet.getString("description"))
                                        .append("price", resultSet.getBigDecimal("price"))
                                        .append("category", resultSet.getString("category"))
                                        .append("quantity", quantity);
                                productDetails.add(product);
                            } else {
                                // Product not found in the database
                                throw new IllegalArgumentException("Product with ID " + productId + " not found in the database");
                            }
                        }
                    }
                }
            }

            // Create the order document
            Document order = new Document("id_buyer", IdBuyer)
                    .append("content", productDetails)
                    .append("pickup", pickupTime.toString())
                    .append("comment", comment)
                    .append("status", "pending")
                    .append("created_at", LocalDateTime.now().toString());

            // Debug
            System.out.println("Attempting to insert order into MongoDB: " + order.toJson());

            // Insert the order into MongoDB
            ordersCollection.insertOne(order);

            // Debug
            System.out.println("Order successfully inserted into MongoDB.");
            return true; // Order successfully created
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating order", e);
        }
    }

    // Check if the time slot is already booked
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

    public List<Order> findOrdersByUserId(String userId) {
        return ordersCollection.find(new Document("id_buyer", userId))
                .map(document -> {
                    try {
                        Order order = new Order();

                        // ObjectId and set it as the id
                        ObjectId objectId = document.getObjectId("_id");
                        if (objectId != null) {
                            String objectIdString = objectId.toHexString();
                            System.out.println("Retrieved ObjectId: " + objectIdString); // Debugging log
                            order.setId(objectId);
                        } else {
                            System.out.println("ObjectId is null");
                        }

                        order.setIdBuyer(document.getString("id_buyer"));

                        // content from Map to Product objects
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> content = (List<Map<String, Object>>) document.get("content");
                        List<Product> products = new ArrayList<>();
                        if (content != null) {
                            for (Map<String, Object> item : content) {
                                Product product = new Product();
                                product.setId((Integer) item.get("id"));
                                product.setProductName((String) item.get("name"));
                                product.setDescription((String) item.get("description"));
                                product.setPrice(Double.valueOf(item.get("price").toString()));
                                product.setCategory((String) item.get("category"));
                                product.setQuantity((Integer) item.get("quantity"));
                                order.setStatus(document.getString("status"));
                                products.add(product);
                            }
                        }
                        order.setContent(products);

                        String pickupTimeStr = document.getString("pickup");
                        if (pickupTimeStr != null) {
                            order.setDateTime(LocalDateTime.parse(pickupTimeStr));
                        }

                        // comment if default = null
                        order.setComment(document.getString("comment") != null ? document.getString("comment") : "");

                        return order;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .into(new ArrayList<>())
                .stream()
                .filter(order -> order != null)
                .collect(Collectors.toList());
    }

    public List<Order> findAllOrders() {
        List<Order> orders = new ArrayList<>();
        for (Document document : ordersCollection.find()) {
            // convert Document to Order object
            Order order = new Order();

            ObjectId objectId = document.getObjectId("_id");
            if (objectId != null) {
                order.setId(objectId);
            }

            order.setIdBuyer(document.getString("id_buyer"));
            order.setContent((List) document.get("content"));
            order.setComment(document.getString("comment"));
            order.setDateTime(LocalDateTime.parse(document.getString("pickup")));
            order.setStatus(document.getString("status"));
            orders.add(order);
        }
        return orders;
    }

    public boolean updateOrderStatus(String orderId, String newStatus) {
        try {
            ObjectId objectId = new ObjectId(orderId);
            Document update = new Document("$set", new Document("status", newStatus));
            Document result = ordersCollection.findOneAndUpdate(new Document("_id", objectId), update);
            return result != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order findOrderById(String orderId) {
        try {
            ObjectId objectId = new ObjectId(orderId);
            Document document = ordersCollection.find(new Document("_id", objectId)).first();
            if (document == null) {
                return null;
            }

            Order order = new Order();
            order.setId(objectId);
            order.setIdBuyer(document.getString("id_buyer"));
            order.setStatus(document.getString("status"));

            return order;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Optional<Order> findById(String orderId) {
        try {
            ObjectId objectId = new ObjectId(orderId);
            Document document = ordersCollection.find(new Document("_id", objectId)).first();
            if (document == null) {
                return Optional.empty();
            }

            Order order = new Order();
            order.setId(objectId);
            order.setIdBuyer(document.getString("id_buyer"));
            order.setStatus(document.getString("status"));

            // Convert content from Document to List<Product>
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) document.get("content");
            List<Product> products = new ArrayList<>();
            if (content != null) {
                for (Map<String, Object> item : content) {
                    Product product = new Product();
                    product.setId((Integer) item.get("id"));
                    product.setProductName((String) item.get("name"));
                    product.setDescription((String) item.get("description"));
                    product.setPrice(Double.valueOf(item.get("price").toString()));
                    product.setCategory((String) item.get("category"));
                    product.setQuantity((Integer) item.get("quantity"));
                    products.add(product);
                }
            }
            order.setContent(products);

            String pickupTimeStr = document.getString("pickup");
            if (pickupTimeStr != null) {
                order.setDateTime(LocalDateTime.parse(pickupTimeStr));
            }

            order.setComment(document.getString("comment") != null ? document.getString("comment") : "");

            return Optional.of(order);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
