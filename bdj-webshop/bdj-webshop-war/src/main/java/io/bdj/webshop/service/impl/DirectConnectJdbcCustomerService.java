package io.bdj.webshop.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;
import java.util.stream.Stream;

import io.bdj.webshop.model.Customer;
import io.bdj.webshop.service.CustomerService;
import io.bdj.webshop.service.ServiceException;

/**
 *
 */
public class DirectConnectJdbcCustomerService extends DirectConnectJdbcService implements CustomerService {

    @Override
    public Customer getCustomerByUserId(final String customerId) throws ServiceException {

        //TODO PERF GMUE don't close connection to cause a connection leak
        try (Connection conn = openConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * from BOUTIQUE.USERS WHERE username = ?")) {

            statement.setString(1, customerId);

            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Customer cust = new Customer();
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
        } catch (SQLException e) {
            throw new ServiceException("Could not read customer", e);
        }

        return null;
    }

    @Override
    public void updateCustomer(final Customer customer) throws ServiceException {

        try (Connection conn = openConnection();
             PreparedStatement statement = conn.prepareStatement("UPDATE BOUTIQUE.USERS "
                                                                         + " SET lastname=?, firstname=?, email=?, street=?, city=?, zip=?, country=?"
                                                                         + " WHERE username = ?")) {

            statement.setString(1, customer.getLastname());
            statement.setString(2, customer.getFirstname());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getStreet());
            statement.setString(5, customer.getCity());
            statement.setString(6, customer.getZip());
            statement.setString(7, customer.getCountry());
            statement.setString(8, customer.getUsername());

            try {
                statement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new ServiceException("User registration failed", e);
            }
        } catch (SQLException e) {
            throw new ServiceException("Could not update customer", e);
        }
    }

    @Override
    public void createCustomer(final Customer newCustomer) throws ServiceException {

        try (Connection conn = openConnection();
             PreparedStatement userSearch = conn.prepareStatement("SELECT COUNT (1) AS total FROM BOUTIQUE.USERS WHERE "
                                                                          + "username = ?");
             Statement stmt = conn.createStatement()) {

            userSearch.setString(1, newCustomer.getUsername());
            ResultSet result = userSearch.executeQuery();
            if (result.next() && result.getInt("total") == 0) {

                String values = Stream.of(newCustomer.getUsername(),
                                          newCustomer.getPassword(),
                                          newCustomer.getLastname(),
                                          newCustomer.getFirstname(),
                                          newCustomer.getEmail(),
                                          newCustomer.getStreet(),
                                          newCustomer.getCity(),
                                          newCustomer.getZip(),
                                          newCustomer.getCountry())
                                      .map(s -> '\'' + s + '\'')
                                      .collect(() -> new StringJoiner(","), StringJoiner::add, StringJoiner::merge)
                                      .toString();

                String userInsert = String.format("INSERT INTO BOUTIQUE.USERS VALUES (%s)", values);
                String roleInsert = String.format("INSERT INTO BOUTIQUE.ROLES (role, username) VALUES ('users', '%s')",
                                                  newCustomer.getUsername());
                try {
                    stmt.executeUpdate(userInsert);
                    stmt.executeUpdate(roleInsert);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new ServiceException("Customer creation failed", e);
                }
            } else {
                throw new ServiceException("User already exists");
            }

        } catch (SQLException e) {
            throw new ServiceException("Could not create user", e);
        }

    }
}
