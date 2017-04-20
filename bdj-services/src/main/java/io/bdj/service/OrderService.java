package io.bdj.service;

import java.util.Map;

import io.bdj.model.Customer;
import io.bdj.model.Product;

/**
 *
 */
public interface OrderService {

    void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total);
}
