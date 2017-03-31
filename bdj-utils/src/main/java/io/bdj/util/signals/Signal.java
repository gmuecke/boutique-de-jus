package io.bdj.util.signals;

import static java.util.logging.Logger.getLogger;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Signals that could be sent between the services.
 */
public enum Signal {

    /**
     * Indicates a positive acknowledge
     */
    ACK(new byte[] { 0x06, 0x06 }),
    /**
     * Indicates a negative acknowledge
     */
    NAK(new byte[] { 0x15, 0x15 }),
    /**
     * Shutdown a service
     */
    SHUTDOWN(new byte[] { 0x1c, 0x1c }),
    /**
     * Queries the status of a service
     */
    QUERY_STATUS(new byte[] { 0x05, 0x53 }),
    /**
     * Indicates a service is starting
     */
    STARTING(new byte[] { 0x53, 0x53 }),
    /**
     * Indicates a service is running and OK
     */
    OK(new byte[] { 0x4f, 0x4b }),;

    private static final Logger LOG = getLogger(Signal.class.getName());

    final byte[] byteSequence;

    Signal(final byte[] byteSequence) {

        this.byteSequence = byteSequence;
    }

    /**
     * Reads a signal from the given datagram.
     *
     * @param data
     *         the packet's data to convert to a signal
     *
     * @return the signal represented by the datagram
     *
     * @throws java.lang.IllegalArgumentException
     *         if the datagram doesn't match any signal
     */
    public static Signal from(byte[] data) {

        for (Signal s : values()) {
            if (s.byteSequence[0] == data[0] && s.byteSequence[1] == data[1]) {
                return s;
            }
        }
        return null;
    }

    /**
     * Reads a signal from the given datagram.
     *
     * @param buf
     *         the packet to convert to a signal
     *
     * @return the signal represented by the datagram
     *
     * @throws java.lang.IllegalArgumentException
     *         if the datagram doesn't match any signal
     */
    public static Signal from(ByteBuffer buf) {

        byte[] data = new byte[2];
        buf.get(data);
        return from(data);
    }

}
