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

    public Product create(String name, String description, Integer quantity, double price, String category) throws SQLException {
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
                    generetedKeys.getLong(1);
                    return new Product(name, description, quantity, price, category);
                }
            }
        }
        return null;
    }

    public boolean edit(Product product) throws SQLException{
        StringBuilder query = new StringBuilder("UPDATE product SET ");
        List<Object> params = new ArrayList<>();
        String name = product.getName();
        String description = product.getDescription();
        Integer quantity = product.getQuantity();
        Double price = product.getPrice();
        String category = product.getCategory();

        boolean firstField = true;

        // Verifica se 'name' è stato passato e se sì, aggiunge alla query
        if(name != null) {
            if(!firstField) query.append(" ,");
            query.append("productName = ?");
            params.add(name);
            firstField = false;
        }

        // Verifica se 'description' è stato passato e se sì, aggiunge alla query
        if(description != null){
            if (!firstField) query.append(", ");
            query.append("description = ?");
            params.add(description);
            firstField = false;
        }

        // Verifica se 'quantity' è stato passato e se sì, aggiunge alla query
        if (quantity != null) {
            if (!firstField) query.append(", ");
            query.append("quantity = ?");
            params.add(quantity);
            firstField = false;
        }

        // Verifica se 'price' è stato passato e se sì, aggiunge alla query
        if (price != null) {
            if (!firstField) query.append(", ");
            query.append("price = ?");
            params.add(price);
            firstField = false;
        }

        // Verifica se 'category' è stato passato e se sì, aggiunge alla query
        if (category != null) {
            if (!firstField) query.append(", ");
            query.append("category = ?");
            params.add(category);
        }

        query.append("WHERE id=?");
        params.add(product.getId());

        System.out.println(query.toString());

        try(Connection c = dataSource.getConnection()){
            try(PreparedStatement statement = c.prepareStatement(query.toString())){
                for (int i = 0; i < params.size(); i++){
                    statement.setObject(i +1, params.get(i));
                }
                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0;
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
}
