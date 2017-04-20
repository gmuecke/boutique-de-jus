package io.bdj.service;

import java.util.Map;

import io.bdj.model.Customer;
import io.bdj.model.Product;

/**
 *
 */
public class PrintOrderService implements OrderService {

    public void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        System.out.println("Submitting order: " + customer+ " " + cart + " " + total);


    }
}
