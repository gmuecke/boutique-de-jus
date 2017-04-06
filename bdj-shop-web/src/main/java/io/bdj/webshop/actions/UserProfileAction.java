package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class UserProfileAction extends ActionSupport implements RequestAware{

    private static final Logger LOG = getLogger(UserProfileAction.class);

    String dbURL = "jdbc:derby://localhost:1527/testdb";
    private String lastname;
    private String firstname;
    private String email;
    private String street;
    private String city;
    private String zip;
    private String country;
    private String username;
    private String password;
    private Map<String, Object> request;

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
                this.username = resultSet.getString("USERNAME");
                this.firstname = resultSet.getString("FIRSTNAME");
                this.lastname = resultSet.getString("LASTNAME");
                this.email = resultSet.getString("EMAIL");
                this.street = resultSet.getString("STREET");
                this.city = resultSet.getString("CITY");
                this.zip = resultSet.getString("ZIP");
                this.country = resultSet.getString("COUNTRY");
                this.password = resultSet.getString("PASSWORD");
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

            statement.setString(2, lastname);
            statement.setString(3, firstname);
            statement.setString(4, email);
            statement.setString(5, street);
            statement.setString(6, city);
            statement.setString(7, zip);
            statement.setString(8, country);
            statement.setString(9, currentUser);

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

    public String getLastname() {

        return lastname;
    }

    public void setLastname(final String lastname) {

        this.lastname = lastname;
    }

    public String getFirstname() {

        return firstname;
    }

    public void setFirstname(final String firstname) {

        this.firstname = firstname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    public String getStreet() {

        return street;
    }

    public void setStreet(final String street) {

        this.street = street;
    }

    public String getCity() {

        return city;
    }

    public void setCity(final String city) {

        this.city = city;
    }

    public String getZip() {

        return zip;
    }

    public void setZip(final String zip) {

        this.zip = zip;
    }

    public String getCountry() {

        return country;
    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(final String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    @Override
    public void setRequest(final Map<String, Object> request) {
        this.request = request;
    }
}
