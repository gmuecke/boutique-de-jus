package io.bdj.webshop.actions;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Cart;
import io.bdj.model.Customer;
import io.bdj.model.Product;
import io.bdj.service.OrderService;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 */
public class OrderAction extends ActionSupport implements SessionAware {

    private Map<String, Object> session;
    private String dbURL = "jdbc:derby://localhost:1527/testdb";
    private Map<Product, Integer> cart = new HashMap<>();
    private Double total;
    private Customer customer;

    public String submit() throws Exception {
        //load all data
        show();

        submitOrder(this.customer, this.cart, this.total);

        return SUCCESS;
    }

    public String show() throws Exception {

        this.customer = readCustomer();

        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();

        Cart sessionCart = (Cart) session.get("cart");

        if (sessionCart == null) {
            //TODO display cart is empty something message
            return SUCCESS;
        }

        this.cart.putAll(sessionCart.getProducts()
                                    .stream()
                                    .collect(Collectors.toMap(this::readProduct, Cart.CartEntry::getQuantity)));

        this.total = this.cart.entrySet()
                              .stream()
                              .collect(Collectors.summingDouble(e -> e.getKey().getPrice() * e.getValue()));

        return SUCCESS;
    }

    private void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total)
            throws NamingException {
        InitialContext ic = new InitialContext();
        OrderService orderService = (OrderService) ic.lookup("java:comp/env/orderService");

        orderService.submitOrder(customer, cart, total);
    }

    private Customer readCustomer() throws Exception {

        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement statement = conn.prepareStatement("SELECT * from BOUTIQUE.USERS WHERE username = ?")) {

            final String currentUser = ServletActionContext.getRequest().getRemoteUser();
            statement.setString(1, currentUser);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Customer cust = new Customer();
                cust.setUsername(resultSet.getString("USERNAME"));
                cust.setFirstname(resultSet.getString("FIRSTNAME"));
                cust.setLastname(resultSet.getString("LASTNAME"));
                cust.setEmail(resultSet.getString("EMAIL"));
                cust.setStreet(resultSet.getString("STREET"));
                cust.setCity(resultSet.getString("CITY"));
                cust.setZip(resultSet.getString("ZIP"));
                cust.setCountry(resultSet.getString("COUNTRY"));
                cust.setPassword(resultSet.getString("PASSWORD"));
                return cust;
            }
        }

        return null;
    }

    private Product readProduct(final Cart.CartEntry ce) {

        //PERF BUG GMUE open a connection per item in the stream
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS WHERE ID = ?")) {

            statement.setString(1, ce.getProductId());
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {

                Product prod = new Product();
                prod.setId(rs.getInt("id"));
                prod.setName(rs.getString("pname"));
                prod.setDescription(rs.getString("description"));
                prod.setCategory(rs.getString("category"));
                prod.setTags(Arrays.asList(rs.getString("tags").split(",")));
                //TODO PERF IDEA include unneeded images
                prod.setImage(rs.getBytes("image"));
                prod.setPrice(rs.getDouble("price"));
                return prod;
            }
            throw new RuntimeException("Could not find product with id " + ce.getProductId());
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch product details", e);
        }
    }

    public Customer getCustomer() {

        return customer;
    }

    @Override
    public void setSession(final Map<String, Object> session) {

        this.session = session;
    }

    public Map<Product, Integer> getCart() {

        return cart;
    }

    public Double getTotal() {

        return total;
    }
}
