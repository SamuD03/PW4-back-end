package its.incom.webdev.rest.model;

import its.incom.webdev.persistence.model.Ingredient;
import its.incom.webdev.persistence.model.Product;

import java.util.Set;
import java.util.stream.Collectors;

public class ProductResponse {
    private Integer id;
    private String productName;
    private String description;
    private Integer quantity;
    private Double price;
    private String category;
    private Set<String> ingredients;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.quantity = product.getQuantity();
        this.price = product.getPrice();
        this.category = product.getCategory();
        this.ingredients = product.getIngredients().stream()
                .map(Ingredient::getName)  // Assuming `Ingredient` has a `getName()` method
                .collect(Collectors.toSet());
    }

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

    public Set<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<String> ingredients) {
        this.ingredients = ingredients;
    }
}
