package io.bdj.util;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to send signals to the services of the Boutique. Each service is implemented to accept signals
 * via UDP to control the services such as shutdown, restart or setting the performance flags remotely. This
 * utility provides the means the send and onReceive the signals.
 */
public enum Signal {

    /**
     * Indicates a positive acknowledge
     */
    ACK(new byte[] { 0x01, 0x06, 0x06, 0x04 }),
    /**
     * Indicates a negative acknowledge
     */
    NAK(new byte[] { 0x01, 0x15, 0x15, 0x4 }),
    /**
     * Shutdown a service
     */
    SHUTDOWN(new byte[] { 0x01, 0x1c, 0x1c, 0x4 }),
    /**
     * Queries the status of a service
     */
    QUERY_STATUS(new byte[] { 0x01, 0x05, 0x53, 0x4 }),
    /**
     * Indicates a service is starting
     */
    STARTING(new byte[] { 0x01, 0x53, 0x53, 0x4 }),
    /**
     * Indicates a service is running and OK
     */
    OK(new byte[] { 0x01, 0x4f, 0x4b, 0x4 }),;

    private static final Logger LOG = getLogger(Signal.class.getName());

    final byte[] byteSequence;

    Signal(final byte[] byteSequence) {

        this.byteSequence = byteSequence;
    }

    /**
     * Reads a signal from the given datagram.
     *
     * @param packet
     *         the packet to convert to a signal
     *
     * @return the signal represented by the datagram
     *
     * @throws java.lang.IllegalArgumentException
     *         if the datagram doesn't match any signal
     */
    public static Signal from(DatagramPacket packet) {

        return from(packet.getData());
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

        outer:
        for (Signal s : values()) {
            byte[] smaller, larger;
            if (data.length > s.byteSequence.length) {
                smaller = s.byteSequence;
                larger = data;
            } else {
                smaller = data;
                larger = s.byteSequence;
            }
            if (compare(smaller, larger)) {
                return s;
            }
        }
        return null;
    }

    private static boolean compare(final byte[] smaller, final byte[] larger) {

        for (int i = 0; i < smaller.length; i++) {
            if (smaller[i] != larger[i]) {
                return false;
            }
        }
        return true;
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

        int size = buf.limit();
        byte[] data = new byte[size];
        buf.get(data);
        return from(data);
    }

    /**
     * Sends the signal to the specified destination
     *
     * @param dst
     *         the destination address to send the signal to
     * @param port
     *         the destination port to send the signal to
     *
     * @return <code>true</code> if the signal could be sent, false if an error occurred
     */
    public boolean send(InetAddress dst, int port, Supplier<DatagramSocket> socketProvider) {

        DatagramPacket packet = new DatagramPacket(this.byteSequence, this.byteSequence.length, dst, port);
        try {
            socketProvider.get().send(packet);
        } catch (IOException e) {
            LOG.log(Level.WARNING, e, () -> "Could not send datagram to " + dst);
            return false;
        }
        return true;
    }
}
