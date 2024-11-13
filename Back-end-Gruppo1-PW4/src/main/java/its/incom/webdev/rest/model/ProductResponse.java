package its.incom.webdev.rest.model;

import its.incom.webdev.persistence.model.Ingredient;
import its.incom.webdev.persistence.model.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductResponse {
    private Integer id;
    private String productName;
    private String description;
    private Integer quantity;
    private Double price;
    private String category;
    private String url;
    private List<IngredientResponse> ingredients;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.quantity = product.getQuantity();
        this.price = product.getPrice();
        this.category = product.getCategory();
        this.url = product.getUrl();
        this.ingredients = product.getIngredients().stream()
                .map(ingredient -> new IngredientResponse(ingredient.getId(), ingredient.getName()))
                .collect(Collectors.toList());
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<IngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    // inner class to represent an ingredient with id and name
    public static class IngredientResponse {
        private Integer id;
        private String name;

        public IngredientResponse(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
