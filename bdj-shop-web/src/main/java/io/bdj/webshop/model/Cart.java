package io.bdj.webshop.model;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Cart {

    /* PERF
        the use of a (not-threadsafe) HashMap here is intentional. The this cart model is stored in the session.
        The session may be accessed in multiple requests concurrently (i.e. through tabbed browsing or ajax)
        So there'll be a chance of creating a stuck thread by concurrently updating the shopping cart.
     */
    private Map<String, Integer> cartContent = new HashMap<>();

    public void addProduct(String id) {
        addProduct(id, 1);
    }

    public void addProduct(String id, int quantity) {
        //of course it's not thread safe, every user owns its own car, so no concurrent access there, eh? ;)
        int value;
        if (cartContent.containsKey(id)) {
            value = cartContent.get(id);
        } else {
            value = 0;
        }
        cartContent.put(id, value + quantity);
    }

    public List<CartEntry> getProducts() {

        return this.cartContent.entrySet().stream().map(e -> new CartEntry(e.getKey(), e.getValue())).collect(toList());
    }

    public static class CartEntry {
        private final String productId;
        private final int quantiy;

        public CartEntry(final String productId, final int quantiy) {

            this.productId = productId;
            this.quantiy = quantiy;
        }

        public String getProductId() {

            return productId;
        }

        public int getQuantity() {

            return quantiy;
        }
    }
}
