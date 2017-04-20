package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Customer;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class UserProfileAction extends ActionSupport implements RequestAware{

    private static final Logger LOG = getLogger(UserProfileAction.class);

    private String dbURL = "jdbc:derby://localhost:1527/testdb";
    private Map<String, Object> request;
    private Customer customer;

    @Override
    public String execute() throws Exception {

        //TODO PERF IDEA don't use a prepared statement here
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
                this.customer = cust;
            }
        }

        return SUCCESS;
    }

    public String save() throws Exception {

        //TODO PERF IDEA don't use prepared statement
        //PERF reuse connection
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement statement = conn.prepareStatement("UPDATE BOUTIQUE.USERS "
                                                                         + " SET lastname=?, firstname=?, email=?, street=?, city=?, zip=?, country=?"
                                                                         + " WHERE username = ?")) {

            final String currentUser = ServletActionContext.getRequest().getRemoteUser();

            statement.setString(1, customer.getLastname());
            statement.setString(2, customer.getFirstname());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getStreet());
            statement.setString(5, customer.getCity());
            statement.setString(6, customer.getZip());
            statement.setString(7, customer.getCountry());
            statement.setString(8, currentUser);

            try {
                statement.executeUpdate();
                conn.commit();
                request.put("result", "User Updated");
            } catch (SQLException e) {
                conn.rollback();
                request.put("result", "Update failed");
                LOG.error("User registration failed", e);
            }
        }
        return SUCCESS;
    }

    public Customer getCustomer() {

        return customer;
    }

    public void setCustomer(final Customer customer) {

        this.customer = customer;
    }

    @Override
    public void setRequest(final Map<String, Object> request) {
        this.request = request;
    }
}
