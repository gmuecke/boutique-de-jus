package io.bdj.service;

import io.bdj.model.Customer;

/**
 * Interface to get customer data into an from the database.
 * <p>
 * This interface allows to replace implementation with different performance characteristics
 * </p>
 */
@ServiceAddress("java:comp/env/customerService")
public interface CustomerService {

    /**
     * Retrieves the customer with the specified customer id
     *
     * @param customerId
     *
     * @return the instance for the customer that matched the ID or null if no customer can be found
     */
    Customer getCustomerByUserId(String customerId) throws ServiceException;

    /**
     * Updates an existing customer with the data of the updated customer. The username of the customer must be set and
     * a user with that id must exist in the database.
     *
     * @param updatedCustomer
     *         the customer with new values.
     */
    void updateCustomer(Customer updatedCustomer) throws ServiceException;

    /**
     * Creates a new customer. The user id of the customer must not exist in the database in order to succeed with
     * this call.
     *
     * @param newCustomer
     *         the new customer to be created
     */
    void createCustomer(Customer newCustomer) throws ServiceException;

}
