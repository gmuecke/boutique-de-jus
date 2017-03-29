package io.bdj.util;

import static java.util.function.Function.identity;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SignalTransceiver implements AutoCloseable {

    public static final InetAddress LOCALHOST;
    private static final Logger LOG = getLogger(SignalTransceiver.class.getName());

    static {
        try {
            LOCALHOST = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Could not create localhost", e);
        }
    }

    private final ExecutorService pool;
    private final Map<Signal, List<Consumer<Event>>> consumers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int listenPort;
    private final DatagramChannel receiverChannel;
    private final DatagramChannel sendChannel;
    private final InetAddress address;

    public SignalTransceiver(InetAddress thisAddr, final int listenPort) {

        this.address = thisAddr;
        this.listenPort = listenPort;
        try {
            this.sendChannel = DatagramChannel.open();
            this.receiverChannel = DatagramChannel.open();
            this.receiverChannel.configureBlocking(false);
            this.receiverChannel.bind(new InetSocketAddress(thisAddr, listenPort));
        } catch (IOException e) {
            throw new RuntimeException("Could not open datagram channel", e);
        }
        this.consumers = Arrays.stream(Signal.values()).collect(toMap(identity(), s -> new CopyOnWriteArrayList<>()));

        final CountDownLatch latch = new CountDownLatch(1);
        this.pool = Executors.newFixedThreadPool(1);
        this.pool.submit(() -> {

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            this.running.set(true);
            latch.countDown();
            while (this.running.get()) {

                try {
                    SocketAddress src = this.receiverChannel.receive(buffer);
                    if(buffer.position() > 0) {
                        buffer.flip();
                        Optional.ofNullable(Event.fromBuffer(buffer))
                                .ifPresent(event -> this.consumers.get(event.getSignal()).forEach(c -> c.accept(event)));
                        buffer.clear();
                    }
                } catch (IOException e) {
                    LOG.log(Level.WARNING, e, () -> "Could not receive package");
                } catch(Exception e){
                }

            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            //ignore
        }

    }

    public static SignalTransceiver create(int listenPort) {

        return create(LOCALHOST, listenPort);
    }

    public static SignalTransceiver create(InetAddress address, int listenPort) {

        return new SignalTransceiver(address, listenPort);
    }

    public boolean send(Signal signal, SocketAddress dst) {
        return send(Event.from(signal), dst);
    }

    public boolean send(Signal signal, byte[] payload, SocketAddress dst) {
        return send(Event.from(signal, payload), dst);
    }

    public boolean send(Signal signal, String payload, SocketAddress dst) {
        return send(Event.from(signal, payload.getBytes()), dst);
    }

    public boolean send(final Event event, SocketAddress dst) {

        try {
            final Event evt;
            if (event.hasSource()) {
                evt = event;
            } else {
                evt = Event.from(event, this.address, this.listenPort);
            }
            this.sendChannel.send(ByteBuffer.wrap(evt.toBytes()), dst);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not send signal", e);
            return false;
        }
    }

    public void onReceive(Signal signal, Consumer<Event> consumer) {

        this.consumers.get(signal).add(consumer);
    }

    @Override
    public void close() throws Exception {

        this.running.set(false);
        try (DatagramChannel ch1 = this.receiverChannel;
             DatagramChannel ch2 = this.sendChannel) {
        } catch (Exception e) {
            //ignore
        }
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }
}
