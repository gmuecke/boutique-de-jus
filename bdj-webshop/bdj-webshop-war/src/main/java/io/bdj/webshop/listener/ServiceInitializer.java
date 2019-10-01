package io.bdj.webshop.listener;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.bdj.webshop.service.CustomerService;
import io.bdj.webshop.service.OrderService;
import io.bdj.webshop.service.ProductService;
import io.bdj.webshop.service.Services;
import io.bdj.webshop.service.impl.DirectConnectJdbcCustomerService;
import io.bdj.webshop.service.impl.DirectConnectJdbcProductService;
import io.bdj.webshop.service.impl.QueuedPrintOrderService;
import org.slf4j.Logger;

/**
 *
 */
public class ServiceInitializer implements ServletContextListener {

    private static final Logger LOG = getLogger(ServiceInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        Services.bind(OrderService.class, new QueuedPrintOrderService());
        Services.bind(CustomerService.class, new DirectConnectJdbcCustomerService());
        Services.bind(ProductService.class, new DirectConnectJdbcProductService());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }


}
