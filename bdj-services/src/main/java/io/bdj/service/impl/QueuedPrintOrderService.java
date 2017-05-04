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

    //TODO PERF limit queue size (parameter)
    private static final int PRINTER_COUNT = 1;
    //TODO PERF make job size parameterizable
    private static final int PRINT_JOBSIZE = 256 * 1024;
    //TODO PERF make print duration parametrizable
    private static final int PRINT_TIME_SECONDS = 60;

    //we have only 1 printer
    //TODO PERF make number of printers configurable
    private static final ScheduledExecutorService SPOOLER = Executors.newScheduledThreadPool(PRINTER_COUNT);

    private final Deque<byte[]> printQueue = new ConcurrentLinkedDeque<>();

    public void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        byte[] printJob = new byte[PRINT_JOBSIZE];
        int offset = 0;
        offset = addToJob(customer, printJob, offset);
        offset = addToJob(cart, printJob, offset);
        addToJob(total, printJob, offset);
        printQueue.push(printJob);

        SPOOLER.schedule(() -> print(printQueue.pop()), 5, TimeUnit.SECONDS);
    }

    /**
     * Of course printing takes some time :)
     * @param pop
     */
    private void print(final byte[] pop) {
        try {
            Thread.sleep(PRINT_TIME_SECONDS * 1000);
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
