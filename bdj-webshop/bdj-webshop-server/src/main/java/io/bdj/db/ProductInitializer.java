package io.bdj.db;

import static org.slf4j.LoggerFactory.getLogger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

import org.slf4j.Logger;

/**
 *
 */
public final class ProductInitializer {

    private static final Logger LOG = getLogger(ProductInitializer.class);
    private ProductInitializer(){}

    public static void populateProducts(final Connection conn) throws SQLException {

        try (Statement stmt = conn.createStatement()) {

            //create the table, if it already exists, an exception is thrown which is ignored but the flow
            //stops at this point
            stmt.executeUpdate(
                    "CREATE TABLE BOUTIQUE.PRODUCTS (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                            + "PNAME VARCHAR(255) , "
                            + "DESCRIPTION VARCHAR(1024), "
                            + "CATEGORY VARCHAR (255), "
                            + "TAGS VARCHAR(255), "
                            + "IMAGE BLOB(4194304), "
                            + "PRICE DECIMAL (8,2)"
                            + ")");
            conn.commit();
            LOG.info("Product table created");
        } catch (SQLException e) {
            if (!DerbyHelper.tableAlreadyExists(e) && !DerbyHelper.schemaAlreadyExists(e)) {
                throw e;
            }
            LOG.info("Product table exists");
        }

        try (PreparedStatement lookup = conn.prepareStatement(
                "SELECT COUNT (1) AS total FROM BOUTIQUE.PRODUCTS WHERE PNAME = ?");
             PreparedStatement insert = conn.prepareStatement(
                     "INSERT INTO BOUTIQUE.PRODUCTS (PNAME, DESCRIPTION, CATEGORY, TAGS, IMAGE, PRICE) VALUES (?, ?, ?, ?, ?, ?)");
             JsonReader reader = Json.createReader(resourceStream("/products.json"))) {

            LOG.info("Populating Product table");
            JsonArray products = reader.readArray();
            products.stream()
                    .map(v -> (JsonObject) v)
                    .filter(p -> isNewProduct(lookup, p.getString("name")))
                    .forEach(p -> {
                        LOG.info("Inserting Product {}", p.toString());
                        try {
                            insert.setString(1, p.getString("name"));
                            insert.setString(2, p.getString("description"));
                            insert.setString(3, p.getString("category"));
                            insert.setString(4,
                                             p.getJsonArray("tags")
                                              .stream()
                                              .map(JsonValue::toString)
                                              .collect(() -> new StringJoiner(","),
                                                       StringJoiner::add,
                                                       StringJoiner::merge)
                                              .toString());
                            insert.setBlob(5, resourceStream(p.getString("image")));
                            insert.setDouble(6, p.getJsonNumber("price").doubleValue());
                            insert.executeUpdate();
                        } catch (SQLException e) {
                            LOG.warn("Unable to prepare statement", e);
                        }
                    });
            conn.commit();
            LOG.info("Product initialization complete");
        } catch (SQLException e) {
            LOG.error("Could not initialize products", e);
        }

    }

    private static InputStream resourceStream(String resource) {

        return ProductInitializer.class.getResourceAsStream(resource);
    }

    /**
     * Checks if the product exists in the database. If no product with the specified name is found, the
     * method returns true;
     *
     * @param lookup
     *         the lookup statement to execute
     * @param name
     *         the name of the product to lookup
     *
     * @return true if no product with that name exists
     */
    private static boolean isNewProduct(final PreparedStatement lookup, final String name) {

        try {
            lookup.setString(1, name);
            try (ResultSet result = lookup.executeQuery()) {

                return (result.next() && result.getInt("total") == 0);
            }

        } catch (SQLException e) {
            LOG.error("Could not find product with name {}", name, e);
        }

        return false;
    }
}
