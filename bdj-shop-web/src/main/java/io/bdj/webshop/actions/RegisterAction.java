package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.RequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class RegisterAction extends ActionSupport implements RequestAware {

    private static final Logger LOG = getLogger(RegisterAction.class);

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

        //TODO validate data
        if(username == null) {
            return SUCCESS;
        }

        String dbURL = "jdbc:derby://localhost:1527/testdb";
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();

        //PERF creating a new connection per request
        //PERF IDEA don't use prepared statements
        //Get a connection
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement userSearch = conn.prepareStatement("SELECT COUNT (1) AS total FROM BOUTIQUE.USERS WHERE "
                                                                          + "username = ?");
             Statement stmt = conn.createStatement()) {

            userSearch.setString(1, username);
            ResultSet result = userSearch.executeQuery();
            if (result.next() && result.getInt("total") == 0) {
                String values = Stream.of(username, password, lastname, firstname, email, street, city, zip, country)
                                      //TODO add SQL injections protection
                                      .map(s -> '\'' + s + '\'')
                                      .collect(() -> new StringJoiner(","), StringJoiner::add, StringJoiner::merge)
                                      .toString();

                String userInsert = String.format("INSERT INTO BOUTIQUE.USERS VALUES (%s)", values);
                String roleInsert = String.format("INSERT INTO BOUTIQUE.ROLES (role, username) VALUES ('users', '%s')",
                                                  username);
                try{
                stmt.executeUpdate(userInsert);
                stmt.executeUpdate(roleInsert);
                conn.commit();
                request.put("result", "User created");
                } catch (SQLException e) {
                    conn.rollback();
                    request.put("result", "Update failed");
                    LOG.error("User registration failed", e);
                }
            } else {
                request.put("result", "User already exists");
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
    public void setRequest(final Map<String, Object> map) {

        this.request = map;
    }
}
