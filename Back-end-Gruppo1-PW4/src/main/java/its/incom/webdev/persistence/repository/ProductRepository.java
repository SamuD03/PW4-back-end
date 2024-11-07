package its.incom.webdev.persistence.repository;

import its.incom.webdev.persistence.model.Product;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProductRepository {
    private DataSource dataSource;

    public ProductRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public Product create(String name, String description, Integer quantity, Double price, String category) throws SQLException {
        try(Connection c = dataSource.getConnection()){
            String query = "INSERT INTO product (productName, description, quantity, price, category) VALUES (?, ?, ?, ?, ?)";
            try(PreparedStatement statement = c.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setString(2, description);
                statement.setInt(3, quantity);
                statement.setDouble(4, price);
                statement.setString(5, category);
                statement.executeUpdate();
                ResultSet generetedKeys = statement.getGeneratedKeys();
                if(generetedKeys.next()){
                    Long id = generetedKeys.getLong(1);
                    Product product = new Product(name, description, quantity, price, category);
                    product.setId(id);
                    return product;
                }
            }
        }
        return null;
    }

    public Product edit(Product product) throws SQLException{
        StringBuilder query = new StringBuilder("UPDATE product SET ");
        List<Object> params = new ArrayList<>();
        String name = product.getName();
        String description = product.getDescription();
        Integer quantity = product.getQuantity();
        Double price = product.getPrice();
        String category = product.getCategory();

        boolean firstField = true;

        // Check if 'name' is provided and add to the query
        if (name != null) {
            if (!firstField) query.append(", ");
            query.append("productName = ?");
            params.add(name);
            firstField = false;
        }

        // Check if 'description' is provided and add to the query
        if (description != null) {
            if (!firstField) query.append(", ");
            query.append("description = ?");
            params.add(description);
            firstField = false;
        }

        // Check if 'quantity' is provided and add to the query
        if (quantity != null) {
            if (!firstField) query.append(", ");
            query.append("quantity = ?");
            params.add(quantity);
            firstField = false;
        }

        // Check if 'price' is provided and add to the query
        if (price != null) { // Ensure price is non-zero if mandatory
            if (!firstField) query.append(", ");
            query.append("price = ?");
            params.add(price);
            firstField = false;
        }

        // Check if 'category' is provided and add to the query
        if (category != null) {
            if (!firstField) query.append(", ");
            query.append("category = ?");
            params.add(category);
        }

        // Ensure at least one field was updated
        if (params.isEmpty()) {
            throw new SQLException("Nessun campo da aggiornare");
        }

        query.append(" WHERE id=?");
        params.add(product.getId());

        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement statement = c.prepareStatement(query.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
                int rowsUpdated = statement.executeUpdate();
                if(rowsUpdated > 0){
                    return findByProductId(product.getId());
                } else {
                    throw new SQLException();
                }
            }
        }
    }


    public Product findByProductId(Long id) throws SQLException{
        try(Connection c = dataSource.getConnection()){
            try(PreparedStatement statement = c.prepareStatement("SELECT id, productName, description, quantity, price, category FROM product WHERE id=?")){
                statement.setLong(1, id);
                ResultSet rs = statement.executeQuery();
                if(rs.next()){
                    Product p = new Product();
                    p.setId(rs.getLong("id"));
                    p.setName(rs.getString("productName"));
                    p.setDescription(rs.getString("description"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setPrice(rs.getDouble("price"));
                    p.setCategory(rs.getString("category"));
                    return p;
                } else {
                    return null;
                }
            }
        }
    }

    public void delete(Long productId) throws SQLException {
        try(Connection c = dataSource.getConnection()) {
            try(PreparedStatement statement = c.prepareStatement("DELETE from product WHERE id = ?")){
                statement.setLong(1, productId);
                int affectedRows = statement.executeUpdate();
                if(affectedRows == 0){
                    throw new SQLException("Product not delete. 0 affected rows");
                }
            }
        }
    }
}
