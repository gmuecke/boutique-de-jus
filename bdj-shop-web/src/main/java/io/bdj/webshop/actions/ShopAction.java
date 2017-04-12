package io.bdj.webshop.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.model.Product;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 */
public class ShopAction extends ActionSupport implements SessionAware{

    private String dbURL = "jdbc:derby://localhost:1527/testdb";
    private List<Product> products = new ArrayList<>();
    private Map<String, Object> session;

    @Override
    public String execute() throws Exception {

        return super.execute();
    }

    public String juices() throws Exception {

        return queryProducts("Juice");
    }

    public String accessoires() throws Exception {

        return queryProducts("Accessoires");
    }
    public String books() throws Exception {

        return queryProducts("Books");
    }
    public String courses() throws Exception {

        return queryProducts("Courses");
    }

    public String queryProducts(String category) throws Exception {
        //TODO PERF IDEA don't use a prepared statement here
        //TODO PERF IDEA do caller-side filtering (no where clause)
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS WHERE CATEGORY = ?")) {

            statement.setString(1, category);
            final ResultSet rs = statement.executeQuery();
            while (rs.next()) {

                Product prod = new Product();
                prod.setId(rs.getInt("id"));
                prod.setName(rs.getString("pname"));
                prod.setDescription(rs.getString("description"));
                prod.setCategory(rs.getString("category"));
                prod.setTags(Arrays.asList(rs.getString("tags").split(",")));
                //TODO PERF IDEA include unneeded images
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

    @Override
    public void setSession(final Map<String, Object> session) {
        this.session = session;
    }
}
