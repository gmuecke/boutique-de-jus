package io.bdj.webshop.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Product;
import io.bdj.service.ProductService;
import io.bdj.service.Services;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 */
public class ShopAction extends ActionSupport implements SessionAware{

    private ProductService productService = Services.getService(ProductService.class);
    private List<Product> products = new ArrayList<>();
    private Map<String, Object> session;

    @Override
    public String execute() throws Exception {

        return super.execute();
    }

    public String juices() throws Exception {

        return queryProducts("Juice");
    }

    public String accessoires() throws Exception {

        return queryProducts("Accessoires");
    }
    public String books() throws Exception {

        return queryProducts("Books");
    }
    public String courses() throws Exception {

        return queryProducts("Course");
    }

    public String queryProducts(String category) throws Exception {
        //TODO PERF IDEA do caller-side filtering (no where clause)
        this.products.addAll(productService.findProductsByCategory(category));
        return SUCCESS;

    }

    public List<Product> getProducts() {

        return products;
    }

    public void setProducts(final List<Product> products) {

        this.products = products;
    }

    @Override
    public void setSession(final Map<String, Object> session) {
        this.session = session;
    }
}
