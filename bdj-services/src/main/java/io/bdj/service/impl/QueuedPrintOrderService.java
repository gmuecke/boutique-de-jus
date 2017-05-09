package io.bdj.service.impl;

import static java.util.logging.Logger.getLogger;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import io.bdj.config.ConfigChangeListener;
import io.bdj.config.Configuration;
import io.bdj.model.Customer;
import io.bdj.model.Product;
import io.bdj.service.OrderService;

/**
 * Of course this is not a real print service. This implementation takes the order and generates a "printJob" that
 * is put into the "printQueue". The Queue is emptied with a given rate (which is slow, because its a printer :).
 * So with a high-enough rate, this should lead to an OOM :)
 */
public class QueuedPrintOrderService implements OrderService {

    private static final Logger LOG = getLogger(QueuedPrintOrderService.class.getName());

    private static final AtomicInteger PRINTER_COUNT = //
            new AtomicInteger(Configuration.getInteger("bdj.qpos.count", 1));
    private static final AtomicInteger PRINT_JOBSIZE =//
            new AtomicInteger(Configuration.getInteger("bdj.qpos.jobSize", 1024));
    private static final AtomicInteger PRINT_TIME_SECONDS = //
            new AtomicInteger(Configuration.getInteger("bdj.qpos.printTimeS", 60));
    private static final AtomicReference<ScheduledExecutorService> SPOOLER = //
            new AtomicReference<>(setupSpooler(PRINTER_COUNT.get()));

    private final Deque<byte[]> printQueue = new ConcurrentLinkedDeque<>();

    public QueuedPrintOrderService() {

        Configuration.addListener(ConfigChangeListener.forConfigProperty("bdj.qpos.count", (k, v) -> {
            LOG.info("Reconfiguring spooler for " + v + " printers");
            final int numPrinters = Integer.parseInt(v);
            final ScheduledExecutorService oldSpooler = SPOOLER.get();
            PRINTER_COUNT.set(numPrinters);
            SPOOLER.set(setupSpooler(numPrinters));
            oldSpooler.shutdownNow();
            LOG.info("Old Spooler removed");
        }));
        Configuration.addListener(ConfigChangeListener.forConfigProperty("bdj.qpos.jobSize", (k, v) -> {
            LOG.info("Using new jobsize " + v);
            PRINT_JOBSIZE.set(Integer.parseInt(v));
        }));
        Configuration.addListener(ConfigChangeListener.forConfigProperty("bdj.qpos.printTimeS", (k, v) -> {
            LOG.info("using new print time " + v);
            PRINT_TIME_SECONDS.set(Integer.parseInt(v));
        }));

    }

    private static ScheduledExecutorService setupSpooler(int printers) {

        return Executors.newScheduledThreadPool(printers);
    }

    public void submitOrder(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        byte[] printJob = createPrintJob(customer, cart, total);

        printQueue.push(printJob);

        SPOOLER.get().schedule(() -> print(printQueue.pop()), 5, TimeUnit.SECONDS);
    }

    private byte[] createPrintJob(final Customer customer, final Map<Product, Integer> cart, final Double total) {

        byte[] printJob = new byte[PRINT_JOBSIZE.get()];
        int offset = 0;
        offset = addToJob(customer, printJob, offset);
        offset = addToJob(cart, printJob, offset);
        addToJob(total, printJob, offset);
        return printJob;
    }

    /**
     * Of course printing takes some time :)
     *
     * @param pop
     */
    private void print(final byte[] pop) {

        try {
            Thread.sleep(PRINT_TIME_SECONDS.get() * 1000);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private int addToJob(Object o, byte[] job, int offset) {

        byte[] oData = o.toString().getBytes();
        System.arraycopy(oData, 0, job, offset, oData.length);
        return oData.length;
    }
}
