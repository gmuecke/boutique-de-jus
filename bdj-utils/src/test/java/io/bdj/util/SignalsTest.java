package io.bdj.util;

import java.net.InetSocketAddress;

import org.junit.Test;

/**
 *
 */
public class SignalsTest {

    @Test
    public void testSendReceive() throws Exception {

        SignalReceiver rcv = SignalReceiver.newReceiver(29999);
        SignalSender snd = SignalSender.newSender();

        rcv.onReceive(Signal.OK,(s, a) -> System.out.println("Received " + s + " from " + a));
        snd.send(Signal.OK, new InetSocketAddress("localhost", 29999));


        Thread.sleep(1000);
    }
}
