package io.bdj.util.signals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 *
 */
public class SignalsTest {

    @Test
    public void testSendReceive() throws Exception {

        try(SignalTransceiver comm = SignalTransceiver.create(InetAddress.getLocalHost(), 29999)){

            CountDownLatch latch = new CountDownLatch(2);
            final SocketAddress dst = new InetSocketAddress(InetAddress.getLocalHost(), 29999);
            comm.onReceive(Signal.OK, (e) -> {
                System.out.println("Received " + e);
                latch.countDown();
            });
            comm.start();
            System.out.println("sending event: OK");
            comm.send(Signal.OK, dst);
            System.out.println("sending event: OK");
            comm.send(Signal.OK, "Test".getBytes(),  dst);
            System.out.println("sending event: QUERY_STATUS");
            comm.send(Signal.QUERY_STATUS, "Example", dst);
            latch.await(10, TimeUnit.SECONDS);
        }


    }
}
