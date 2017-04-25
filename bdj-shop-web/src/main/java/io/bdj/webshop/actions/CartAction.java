package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Cart;
import io.bdj.model.Product;
import io.bdj.service.ProductService;
import io.bdj.service.Services;
import org.apache.struts2.interceptor.SessionAware;
import org.slf4j.Logger;

/**
 *
 */
public class CartAction extends ActionSupport implements SessionAware {

    private static final Logger LOG = getLogger(CartAction.class);

    private ProductService productService = Services.getService(ProductService.class);

    private Map<Product, Integer> cart = new HashMap<>();
    private Map<String, Object> session;

    private double total;
    private String url;
    private String id;
    private int quantity;

    @Override
    public String execute() throws Exception {

        Cart sessionCart = (Cart) session.get("cart");
        if (sessionCart == null) {
            //TODO display cart is empty something message
            return SUCCESS;
        }
        this.cart.putAll(sessionCart.getProducts()
                                    .stream()
                                    .collect(Collectors.toMap(productService::readProduct, Cart.CartEntry::getQuantity)));

        this.total = this.cart.entrySet()
                              .stream()
                              .collect(Collectors.summingDouble(e -> e.getKey().getPrice() * e.getValue()));

        return SUCCESS;
    }

    public String add() throws Exception {

        session.putIfAbsent("cart", new Cart());
        Cart cart = (Cart) session.get("cart");
        cart.addProduct(id, quantity);

        return INPUT;
    }

    @Override
    public void setSession(final Map<String, Object> session) {

        this.session = session;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(final String url) {

        this.url = url;
    }

    public String getId() {

        return id;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public int getQuantity() {

        return quantity;
    }

    public void setQuantity(final int quantity) {

        this.quantity = quantity;
    }

    public double getTotal() {

        return total;
    }

    public Map<Product, Integer> getCart() {

        return cart;
    }
}
