package its.incom.webdev.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "orders")
public class Order extends PanacheMongoEntity {
    @BsonProperty("id_buyer")
    private String IdBuyer;

    @BsonProperty("content")
    private List<Product> content;

    @BsonProperty("comment")
    private String comment;

    @BsonProperty("pickup_time")
    private LocalDateTime dateTime;

    public Order() {}

    public Order(String IdBuyer, List<Product> content, String comment, LocalDateTime dateTime) {
        this.IdBuyer = IdBuyer;
        this.content = content;
        this.comment = comment;
        this.dateTime = dateTime;
    }

    // Getters and Setters
    public String getIdBuyer() {
        return IdBuyer;
    }

    public void setIdBuyer(String idBuyer) {
        this.IdBuyer = idBuyer;
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
}
