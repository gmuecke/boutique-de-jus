package io.bdj.user.db;

import static java.util.logging.Logger.getLogger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import org.apache.derby.drda.NetworkServerControl;

/**
 *
 */
public class DerbyStandalone {

    private static final Logger LOG = getLogger(DerbyStandalone.class.getName());

    public static void main(String... args) throws Exception {

        System.setProperty("derby.system.home", "testdb");
        NetworkServerControl server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
        server.start(new PrintWriter(System.out));
        server.setTimeSlice(5);
        server.setMaxThreads(10);

        final int stopPort = Integer.parseInt(System.getProperty("bdj.db.signalPort", "11009"));
        try (SignalTransceiver com = SignalTransceiver.create(stopPort).start()) {
            final AtomicBoolean running = new AtomicBoolean(true);
            com.onReceive(Signal.SHUTDOWN, e -> {
                LOG.info("Received stop signal from " + e.getReplyAddr());
                running.set(false);
            });
            while (running.get()) {
                Thread.sleep(1000);
            }
        }

    }
}
