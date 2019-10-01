package io.bdj.webshop.service;

import java.util.List;

import io.bdj.webshop.model.Cart;
import io.bdj.webshop.model.Product;

/**
 * A data access object interface to retrieve product information from the database.
 * <p>
 * This interface allows to replace implementation with different performance characteristics
 * </p>
 */
@ServiceAddress("java:comp/env/productService")
public interface ProductService {

    /**
     * Searches a product with a specific id.
     * @param productId
     *  the id of product to search
     * @return
     *  the product or null if the product could not be found
     * @throws ServiceException
     *  if the product could not be found for technical reasons
     */
    Product lookupProduct(String productId) throws ServiceException;

    List<Product> findProductsByCategory(String category) throws ServiceException;

    /**
     * Reads the binary image data from the service for the image with the specified id
     * @param id
     *  the product id of the image to be retrieved.
     * @return
     *  the image data
     * @throws ServiceException
     */
    byte[] getImageData(final String id) throws ServiceException;

    /**
     * Finds the product for the specified cart entry. The method throws a {@link ServiceRuntimeException}
     * if the product can either not be found either because it does not exist or for other reasons.
     * @param ce
     *  the cart entry whose product should be fetched
     * @return
     *  the product if it can be found.
     */
    default Product readProduct(final Cart.CartEntry ce) {

        try {
            Product product = lookupProduct(ce.getProductId());
            if (product == null) {
                throw new ServiceRuntimeException("Could not find product with id " + ce.getProductId());
            }
            return product;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Could not fetch product details", e);
        }
    }
}
