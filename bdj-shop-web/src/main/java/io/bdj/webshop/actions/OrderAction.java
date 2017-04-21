package io.bdj.webshop.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Cart;
import io.bdj.model.Customer;
import io.bdj.model.Product;
import io.bdj.service.CustomerService;
import io.bdj.service.OrderService;
import io.bdj.service.ProductService;
import io.bdj.service.Services;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 */
public class OrderAction extends ActionSupport implements SessionAware {

    private ProductService productService = Services.getService(ProductService.class);
    private OrderService orderService = Services.getService(OrderService.class);
    private CustomerService customerService = Services.getService(CustomerService.class);
    private Map<String, Object> session;
    private Map<Product, Integer> cart = new HashMap<>();
    private Double total;
    private Customer customer;

    public String submit() throws Exception {
        //load all data
        show();

        orderService.submitOrder(customer, cart, total);

        return SUCCESS;
    }

    public String show() throws Exception {

        Cart sessionCart = (Cart) session.get("cart");
        if (sessionCart == null) {
            //TODO display cart is empty something message
            return SUCCESS;
        }

        final String currentUser = ServletActionContext.getRequest().getRemoteUser();
        this.customer = this.customerService.getCustomerByUserId(currentUser);

        this.cart.putAll(sessionCart.getProducts()
                                    .stream()
                                    .collect(Collectors.toMap(productService::readProduct, Cart.CartEntry::getQuantity)));

        this.total = this.cart.entrySet()
                              .stream()
                              .collect(Collectors.summingDouble(e -> e.getKey().getPrice() * e.getValue()));

        return SUCCESS;
    }


    public Customer getCustomer() {

        return customer;
    }

    @Override
    public void setSession(final Map<String, Object> session) {

        this.session = session;
    }

    public Map<Product, Integer> getCart() {

        return cart;
    }

    public Double getTotal() {

        return total;
    }
}
