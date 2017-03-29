package io.bdj.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * An Event to be sent and received by a client
 */
public class Event {

    public static final int MAX_PAYLOAD = 1024;

    public static final int SOH = 0x01;
    public static final int SOT = 0x02;
    public static final int EOT = 0x03;
    public static final int EOF = 0x04;
    public static final int US = 0x1f;

    private static final byte[] EMPTY_PAYLOAD = new byte[0];
    private static final InetAddress EMPTY_SOURCE;

    static {
        try {
            EMPTY_SOURCE = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 });
        } catch (UnknownHostException e) {
            throw new RuntimeException("Could not create localhost", e);
        }
    }

    private final Signal signal;
    private final byte[] payload;
    private final InetAddress source;
    private final int callbackPort;

    private Event(final Signal signal, final byte[] payload, final InetAddress source, final int callbackPort) {

        Objects.requireNonNull(signal, "Signal must not be null");
        Objects.requireNonNull(payload, "Payload must not be null");
        Objects.requireNonNull(source, "Source must not be null");
        if (payload.length > MAX_PAYLOAD) {
            throw new IllegalArgumentException("Payload limit exceeded: " + payload.length);
        }
        this.signal = signal;
        this.payload = payload;
        this.source = source;
        this.callbackPort = callbackPort;
    }

    public static Event from(Signal signal) {

        return from(signal, EMPTY_PAYLOAD, EMPTY_SOURCE, -1);
    }

    public static Event from(Signal signal, byte[] payload, InetAddress dst, int port) {

        return new Event(signal, payload, dst, port);
    }

    public static Event from(Signal signal, byte[] payload) {

        return from(signal, payload, EMPTY_SOURCE, -1);
    }

    public static Event from(Event event, InetAddress dst, int port) {

        return from(event.getSignal(), event.getPayload(), dst, port);
    }

    public Signal getSignal() {

        return signal;
    }

    public byte[] getPayload() {

        return payload;
    }

    public static Event fromBytes(byte[] data) {

        ByteBuffer buf = ByteBuffer.wrap(data);
        return fromBuffer(buf);
    }

    public static Event fromBuffer(final ByteBuffer buf) {

        skipUntil(buf, SOH);
        Signal signal = Signal.from(buf);
        failIfNotMatch(buf, US);
        InetAddress sourceInetAddr = readInetAddress(buf);
        failIfNotMatch(buf, US);
        int port = readPort(buf);
        failIfNotMatch(buf, SOT);
        byte[] payload = readBytesUntil(buf, EOT);
        failIfNotMatch(buf, EOT);
        failIfNotMatch(buf, EOF);
        return new Event(signal, payload, sourceInetAddr, port);
    }

    private static byte[] readBytesUntil(final ByteBuffer buf, final int eot) {

        int begin = buf.position();
        int end = findPositionOf(buf, eot);
        byte[] payload = new byte[end - begin - 1];
        buf.get(payload);
        return payload;
    }

    private static int readPort(final ByteBuffer buf) {

        byte[] port = new byte[2];
        buf.get(port);
        return (port[0] << 8) + port[1];
    }

    private static void skipUntil(final ByteBuffer buf, final int stopByte) {

        while (buf.get() != stopByte) {
        }
    }

    private static void failIfNotMatch(final ByteBuffer buf, final int expected) {

        if (buf.get() != expected) {
            throw new IllegalArgumentException("Buffer data is invalid, unexpected byte");
        }
    }

    private static InetAddress readInetAddress(final ByteBuffer buf) {

        try {
            return InetAddress.getByAddress(readBytesUntil(buf, US));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Buffer data is invalid, unknown source addr", e);
        }
    }

    private static int findPositionOf(final ByteBuffer buf, final int stop) {

        buf.mark();
        skipUntil(buf, stop);
        int pos = buf.position();
        buf.reset();
        return pos;
    }

    public SocketAddress getReplyAddr() {

        if (hasSource()) {
            return new InetSocketAddress(this.source, this.callbackPort);
        }
        throw new IllegalStateException("Event has not reply address");
    }

    public boolean hasSource() {

        return this.source != EMPTY_SOURCE && this.callbackPort > 0;
    }

    InetAddress getSource() {

        return source;
    }

    int getCallbackPort() {

        return callbackPort;
    }

    public byte[] toBytes() {

        byte[] sourceAddr = this.source.getAddress();
        byte[] signalData = this.signal.byteSequence;
        byte[] payload = this.payload;
        int port = this.callbackPort;

        byte[] data = new byte[1 + signalData.length + 1 + sourceAddr.length + 1 + 2 + 1 + payload.length + 2];
        int pos = 0;
        data[pos++] = SOH; //start-of-head
        System.arraycopy(signalData, 0, data, pos , signalData.length);
        pos += signalData.length;
        data[pos++] = US; //unit separator
        System.arraycopy(sourceAddr, 0, data, pos , sourceAddr.length);
        pos += sourceAddr.length;
        data[pos++] = US; //unit separator
        data[pos++] = (byte) ((port & 0xff00) >> 8);
        data[pos++] = (byte) ((port & 0xff));
        data[pos++] = SOT; //start-of-text
        if(payload.length > 0){
            System.arraycopy(payload, 0, data, pos , payload.length);
            pos += payload.length;
        }
        data[pos++] = EOT; //end-of-text
        data[pos++] = EOF; //end-of-transmission
        return data;
    }

    @Override
    public String toString() {

        return new StringBuilder(32).append("Event[")
                                    .append(signal)
                                    .append(",src=")
                                    .append(this.source)
                                    .append(':')
                                    .append(this.callbackPort)
                                    .append(Arrays.toString(this.payload))
                                    .append("]")
                                    .toString();
    }
}
