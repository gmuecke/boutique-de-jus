package io.bdj.util.signals;

import static java.net.InetAddress.getByAddress;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class EventTest {

    @Test
    public void eventMarshallUnmarshall() throws Exception {

        Event evt = Event.from(Signal.OK, "Test".getBytes(), getByAddress(new byte[] { 64, 96, 0, 1 }), 29999);
        System.out.println(evt);

        Event evt2 = Event.fromBytes(evt.toBytes());
        System.out.println(evt2);

        assertEquals(evt.toString(), evt2.toString());

    }
}
