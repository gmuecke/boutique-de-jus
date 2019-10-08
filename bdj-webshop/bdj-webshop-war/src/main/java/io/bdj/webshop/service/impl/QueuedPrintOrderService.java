package io.bdj.webshop.service.impl;

import static java.util.logging.Logger.getLogger;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import io.bdj.webshop.model.Customer;
import io.bdj.webshop.model.Product;
import io.bdj.webshop.service.OrderService;

/**
 * Of course this is not a real print service. This implementation takes the order and generates a "printJob" that
 * is put into the "printQueue". The Queue is emptied with a given rate (which is slow, because its a printer :).
 * So with a high-enough rate, this should lead to an OOM :)
 */
public class QueuedPrintOrderService implements OrderService {

    private static final Logger LOG = getLogger(QueuedPrintOrderService.class.getName());


    private final Deque<byte[]> printQueue = new ConcurrentLinkedDeque<>();

    public QueuedPrintOrderService() {


    }

    private static ScheduledExecutorService setupSpooler(int printers) {

        return Executors.newScheduledThreadPool(printers);
    }

    public void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        byte[] printJob = createPrintJob(customer, cart, total);

        printQueue.push(printJob);

    }

    private byte[] createPrintJob(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        return new byte[0];
    }

}
