package io.bdj.service.impl;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.bdj.model.Customer;
import io.bdj.model.Product;
import io.bdj.service.OrderService;

/**
 * Of course this is not a real print service. This implementation takes the order and generates a "printJob" that
 * is put into the "printQueue". The Queue is emptied with a given rate (which is slow, because its a printer :).
 * So with a high-enough rate, this should lead to an OOM :)
 */
public class QueuedPrintOrderService implements OrderService {

    //we have only 1 printer
    //TODO PERF make number of printers configurable
    private static final ScheduledExecutorService SPOOLER = Executors.newScheduledThreadPool(1);

    //TODO PERF limit queue size (parameter)
    private final Deque<byte[]> printQueue = new ConcurrentLinkedDeque<>();

    public void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        //TODO PERF make job size parameterizable
        byte[] printJob = new byte[65536];
        int offset = 0;
        offset = addToJob(customer, printJob, offset);
        offset = addToJob(cart, printJob, offset);
        addToJob(total, printJob, offset);
        printQueue.push(printJob);

        SPOOLER.schedule(() -> {
            print(printQueue.pop());
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * Of course printing takes some time :)
     * @param pop
     */
    private void print(final byte[] pop) {
        try {
            //TODO PERF make print duration parametrizable
            Thread.sleep(30_000); //30 seconds per page
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private int addToJob(Object o, byte[] job, int offset){
        byte[] oData = o.toString().getBytes();
        System.arraycopy(oData,0, job, offset, oData.length);
        return oData.length;
    }
}
