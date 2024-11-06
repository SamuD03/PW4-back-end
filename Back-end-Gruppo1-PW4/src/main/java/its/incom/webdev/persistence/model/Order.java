package its.incom.webdev.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "order")
public class Order extends PanacheMongoEntity {
    private String emailBuyer;
    private List<Product> content;
    private String comment;
    private LocalDateTime dateTime;

    public Order() {}

    public String getEmailBuyer() {
        return emailBuyer;
    }

    public void setEmailBuyer(String emailBuyer) {
        this.emailBuyer = emailBuyer;
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

    public void setPickupTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}

