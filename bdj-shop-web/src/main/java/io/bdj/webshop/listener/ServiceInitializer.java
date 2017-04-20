package io.bdj.webshop.listener;

import static org.slf4j.LoggerFactory.getLogger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.bdj.service.PrintOrderService;
import org.slf4j.Logger;

/**
 *
 */
public class ServiceInitializer implements ServletContextListener {

    private static final Logger LOG = getLogger(ServiceInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            InitialContext ic = new InitialContext();
            ic.bind("java:comp/env/orderService", new PrintOrderService());

        } catch (NamingException e) {
            LOG.warn("Could not initialize services");
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        try {
            InitialContext ic = new InitialContext();
            ic.unbind("services/order");
        } catch (NamingException e) {
            LOG.warn("Could not deregister services");
        }
    }


}
