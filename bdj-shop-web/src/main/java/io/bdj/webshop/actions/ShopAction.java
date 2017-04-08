package io.bdj.webshop.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.model.Product;

/**
 *
 */
public class ShopAction extends ActionSupport {

    private String dbURL = "jdbc:derby://localhost:1527/testdb";
    private List<Product> products = new ArrayList<>();

    @Override
    public String execute() throws Exception {

        //TODO PERF IDEA don't use a prepared statement here
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS")) {

            final ResultSet rs = statement.executeQuery();
            while (rs.next()) {

                Product prod = new Product();
                prod.setId(rs.getInt("id"));
                prod.setName(rs.getString("pname"));
                prod.setDescription(rs.getString("description"));
                prod.setCategory(rs.getString("category"));
                prod.setTags(Arrays.asList(rs.getString("tags").split(",")));
                prod.setImage(rs.getBytes("image"));
                prod.setPrice(rs.getDouble("price"));
                products.add(prod);

            }
        }

        return SUCCESS;
    }

    public List<Product> getProducts() {

        return products;
    }

    public void setProducts(final List<Product> products) {

        this.products = products;
    }
}
