package io.bdj.webshop.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.model.Cart;
import io.bdj.webshop.model.Customer;
import io.bdj.webshop.model.Product;
import io.bdj.webshop.service.CustomerService;
import io.bdj.webshop.service.OrderService;
import io.bdj.webshop.service.ProductService;
import io.bdj.webshop.service.Services;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 */
public class OrderAction extends ActionSupport implements SessionAware, RequestAware {

    private ProductService productService = Services.getService(ProductService.class);
    private OrderService orderService = Services.getService(OrderService.class);
    private CustomerService customerService = Services.getService(CustomerService.class);
    private Map<String, Object> session;
    private Map<String, Object> request;
    private Map<Product, Integer> cart = new HashMap<>();
    private Double total;
    private Customer customer;
    private String order;

    @Override
    public String execute() throws Exception {
        //load all data
        input();

        orderService.submitOrder(customer, cart, total);

        return SUCCESS;
    }

    @Override
    public String input() throws Exception {

        Cart sessionCart = (Cart) session.get("cart");
        if(sessionCart == null){
            return INPUT;
        }


        final String currentUser = ServletActionContext.getRequest().getRemoteUser();
        this.customer = this.customerService.getCustomerByUserId(currentUser);

        this.cart.putAll(sessionCart.getProducts()
                                    .stream()
                                    .collect(Collectors.toMap(productService::readProduct, Cart.CartEntry::getQuantity)));

        this.total = this.cart.entrySet()
                              .stream()
                              .collect(Collectors.summingDouble(e -> e.getKey().getPrice() * e.getValue()));

        return INPUT;
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

    @Override
    public void setRequest(final Map<String, Object> request) {
        this.request = request;
    }
}
