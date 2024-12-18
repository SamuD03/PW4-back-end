package its.incom.webdev.rest.model;

import its.incom.webdev.persistence.repository.IngredientRepository;

import java.util.Set;

public class ProductRequest {
    private String productName;
    private String description;
    private Integer quantity;
    private Double price;
    private String category;
    private Set<String> ingredients;
    private String url;

    private IngredientRepository ingredientRepository;

    public ProductRequest(){}

    public Set<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<String> ingredients) {
        this.ingredients = ingredients;
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

    public IngredientRepository getIngredientRepository() {
        return ingredientRepository;
    }

    public void setIngredientRepository(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }
    public void setUrl(Double price) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
// Getters and Setters for the rest of the fields
}
