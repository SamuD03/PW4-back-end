package its.incom.webdev.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "productName", nullable = false, unique = true)
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "url", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'nan'")
    private String url = "nan";  // Default value

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_ingredient",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients;

    public Product(){}

    public Product(String productName, String description, Integer quantity, Double price, String category) {
        this.productName = productName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.url = "nan";
    }

    // Getters and Setters for id, productName, description, quantity, price, category, and ingredients
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to decrease must be greater than zero");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity available");
        }
        this.quantity -= amount;
    }

}
