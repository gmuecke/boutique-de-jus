package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.model.Customer;
import io.bdj.webshop.service.CustomerService;
import io.bdj.webshop.service.ServiceException;
import io.bdj.webshop.service.Services;
import org.apache.struts2.interceptor.RequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class RegisterAction extends ActionSupport implements RequestAware {

    private static final Logger LOG = getLogger(RegisterAction.class);

    private CustomerService service = Services.getService(CustomerService.class);

    private Customer customer;
    private Map<String, Object> request;

    @Override
    public String input() throws Exception {

        return INPUT;
    }

    @Override
    public String execute() throws Exception {

        try {
            service.createCustomer(this.customer);
            request.put("result", "User created");

        } catch (ServiceException e) {
            request.put("result", e.getMessage());
            LOG.error("User registration failed", e);
            return ERROR;

        }
        return SUCCESS;
    }

    public Customer getCustomer() {

        return customer;
    }

    public void setCustomer(final Customer customer) {

        this.customer = customer;
    }

    @Override
    public void setRequest(final Map<String, Object> map) {

        this.request = map;
    }
}
