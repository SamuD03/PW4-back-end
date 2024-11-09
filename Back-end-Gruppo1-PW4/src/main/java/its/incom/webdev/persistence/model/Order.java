package its.incom.webdev.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@MongoEntity(collection = "orders")
public class Order extends PanacheMongoEntity {

    @BsonProperty("id_buyer")
    private String idBuyer;

    @BsonProperty("content")
    private List<Product> content;

    @BsonProperty("comment")
    private String comment;

    @BsonProperty("pickup_time")
    private LocalDateTime dateTime;

    @BsonProperty("status")
    private String status;

    public Order() {}

    public Order(String idBuyer, List<Product> content, String comment, LocalDateTime dateTime) {
        this.idBuyer = idBuyer;
        this.content = content;
        this.comment = comment;
        this.dateTime = dateTime;
        this.status = status;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getIdBuyer() {
        return idBuyer;
    }

    public void setIdBuyer(String idBuyer) {
        this.idBuyer = idBuyer;
    }

    public List<Product> getContent() {
        return content;
    }

    public void setContent(List<Product> content) {
        this.content = content;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBuyerId() {
        return Integer.parseInt(idBuyer);
    }

    public String getPickupDate() {
        // Format the LocalDateTime object to a readable string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
}