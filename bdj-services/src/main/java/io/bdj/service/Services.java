package io.bdj.service;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Helper class to handle registration and lookup of services used by the webshop.
 */
public final class Services {

    private Services() {

    }

    /**
     * Registers a new service in JNDI. The service is bound to the name define by the service interface's
     * {@link io.bdj.service.ServiceAddress} annotation.
     *
     * @param service
     *         the service instance to be registered. The service must implement an interface that is annotated with
     *         {@link io.bdj.service.ServiceAddress} that defines the service address.
     */
    public static <S, I extends S> void bind(Class<S> serviceClass, I service) {

        ServiceAddress addr = getServiceAddress(serviceClass);
        try {
            InitialContext ic = new InitialContext();
            ic.bind(addr.value(), service);
        } catch (NamingException e) {
            throw new ServiceRuntimeException("Could not bind service " + serviceClass);
        }
    }

    /**
     * Removes the service instance for the specified service class from the naming directory
     * @param serviceClass
     *  the service class interface that defines the {@link io.bdj.service.ServiceAddress}
     */
    public static void unbind(Class<?> serviceClass) {

        ServiceAddress addr = getServiceAddress(serviceClass);
        try {
            InitialContext ic = new InitialContext();
            ic.unbind(addr.value());
        } catch (NamingException e) {
            throw new ServiceRuntimeException("Could not unbind service " + serviceClass);
        }
    }

    private static <S> ServiceAddress getServiceAddress(final Class<S> serviceClass) {

        ServiceAddress addr = serviceClass.getAnnotation(ServiceAddress.class);
        if (addr == null) {
            throw new ServiceRuntimeException("Service has no @ServiceAddress");
        }
        return addr;
    }

    /**
     * Locates the service for the specified service interface
     *
     * @param serviceClass
     *         the service (interface) class for which a service instance should be retrieved. The class must be
     *         annotated with ServiceAddress in order to look it up using JNDI.
     * @param <T>
     *         the type of the service
     *
     * @return the service instance for the interface
     */
    public static <T> T getService(Class<T> serviceClass) {

        ServiceAddress addr = serviceClass.getAnnotation(ServiceAddress.class);
        if (addr == null) {
            throw new ServiceRuntimeException("Service " + serviceClass + " has no JNDI name");
        }

        try {
            InitialContext ic = new InitialContext();
            return (T) ic.lookup(addr.value());
        } catch (NamingException e) {
            throw new ServiceRuntimeException("Could not locate service", e);
        }

    }
}
