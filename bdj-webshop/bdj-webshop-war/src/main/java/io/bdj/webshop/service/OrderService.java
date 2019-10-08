package io.bdj.webshop.service;

import java.util.Map;

import io.bdj.webshop.model.Customer;
import io.bdj.webshop.model.Product;

/**
 *
 */
@ServiceAddress("java:comp/env/orderService")
public interface OrderService {

    void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total);
}
