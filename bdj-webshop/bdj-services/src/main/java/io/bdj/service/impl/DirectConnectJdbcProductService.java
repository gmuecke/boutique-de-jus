package io.bdj.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.bdj.model.Product;
import io.bdj.service.ProductService;
import io.bdj.service.ServiceException;

/**
 *
 */
public class DirectConnectJdbcProductService extends DirectConnectJdbcService implements ProductService {

    @Override
    public Product lookupProduct(final String productId) throws ServiceException {

        try (Connection conn = openConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS WHERE ID = ?")) {

            statement.setString(1, productId);
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {

                Product prod = new Product();
                prod.setId(rs.getInt("id"));
                prod.setName(rs.getString("pname"));
                prod.setDescription(rs.getString("description"));
                prod.setCategory(rs.getString("category"));
                prod.setTags(Arrays.asList(rs.getString("tags").split(",")));
                //TODO PERF images are always fetched, but not always needed
                prod.setImage(rs.getBytes("image"));
                prod.setPrice(rs.getDouble("price"));
                return prod;
            }
            return null;
        } catch (SQLException e) {
            throw new ServiceException("Could not fetch product details", e);
        }
    }

    @Override
    public List<Product> findProductsByCategory(final String category) throws ServiceException {

        List<Product> products = new ArrayList<>();
        try (Connection conn = openConnection();
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
        } catch (SQLException e) {
            throw new ServiceException("Could not fetch products", e);
        }
        return products;
    }

    @Override
    public byte[] getImageData(final String id) throws ServiceException {

        try (Connection conn = openConnection();
             //PERF fetch all columns although we only need 1 image
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS WHERE id = ?")) {

            statement.setString(1, id);
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getBytes("image");
            }
        } catch (SQLException e) {
            throw new ServiceException(e);
        }
        throw new ServiceException("Could not find image with id " + id);
    }
}
