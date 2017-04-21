package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.model.Customer;
import io.bdj.service.CustomerService;
import io.bdj.service.ServiceException;
import io.bdj.service.Services;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class UserProfileAction extends ActionSupport implements RequestAware {

    private static final Logger LOG = getLogger(UserProfileAction.class);

    private CustomerService service = Services.getService(CustomerService.class);

    private Map<String, Object> request;
    private Customer customer;

    @Override
    public String execute() throws Exception {

        final String currentUser = ServletActionContext.getRequest().getRemoteUser();

        this.customer = this.service.getCustomerByUserId(currentUser);

        return SUCCESS;
    }

    public String save() throws Exception {

        final String currentUser = ServletActionContext.getRequest().getRemoteUser();
        this.customer.setUsername(currentUser);

        try {
            this.service.updateCustomer(this.customer);
            request.put("result", "User Updated");
        } catch (ServiceException e) {
            LOG.error("User registration failed", e);
            request.put("result", "Update failed");
        }
        //TODO use proper result instead of message
        return SUCCESS;
    }

    public Customer getCustomer() {

        return customer;
    }

    public void setCustomer(final Customer customer) {

        this.customer = customer;
    }

    @Override
    public void setRequest(final Map<String, Object> request) {

        this.request = request;
    }
}
