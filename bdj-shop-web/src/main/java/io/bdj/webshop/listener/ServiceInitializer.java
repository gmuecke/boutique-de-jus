package io.bdj.webshop.listener;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.bdj.service.CustomerService;
import io.bdj.service.OrderService;
import io.bdj.service.ProductService;
import io.bdj.service.Services;
import io.bdj.service.impl.DirectConnectJdbcCustomerService;
import io.bdj.service.impl.DirectConnectJdbcProductService;
import io.bdj.service.impl.PrintOrderService;
import org.slf4j.Logger;

/**
 *
 */
public class ServiceInitializer implements ServletContextListener {

    private static final Logger LOG = getLogger(ServiceInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        Services.bind(OrderService.class, new PrintOrderService());
        Services.bind(CustomerService.class, new DirectConnectJdbcCustomerService());
        Services.bind(ProductService.class, new DirectConnectJdbcProductService());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        Services.unbind(OrderService.class);
        Services.unbind(CustomerService.class);
        Services.unbind(ProductService.class);
    }


}
