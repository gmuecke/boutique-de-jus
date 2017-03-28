package io.bdj.util;

import static java.util.function.Function.identity;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receiver for signals
 */
public class SignalReceiver implements AutoCloseable {

    private static final Logger LOG = getLogger(SignalReceiver.class.getName());

    private final DatagramChannel channel;
    private final ExecutorService pool;
    private final Map<Signal, List<BiConsumer<Signal, SocketAddress>>> consumers;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private SignalReceiver(final DatagramChannel channel) {

        this.channel = channel;
        //initialize the map
        this.consumers = Arrays.stream(Signal.values()).collect(toMap(identity(), s -> new CopyOnWriteArrayList<>()));

        this.pool = Executors.newFixedThreadPool(1);
        this.pool.submit(() -> {

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            this.running.set(true);
            while (this.running.get()) {

                try {
                    SocketAddress src = channel.receive(buffer);
                    buffer.flip();
                    Optional.ofNullable(Signal.from(buffer))
                            .ifPresent(s -> consumers.get(s).forEach(c -> c.accept(s, src)));
                    buffer.clear();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, e, () -> "Could not receive package");
                }

            }
        });
    }

    public static SignalReceiver newReceiver(int port) {

        try {
            DatagramChannel channel = DatagramChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(port);
            channel.configureBlocking(false);
            channel.bind(socketAddress);
            return new SignalReceiver(channel);
        } catch (IOException e) {
            throw new RuntimeException("Could not open datagram channel", e);
        }
    }

    public void onReceive(Signal signal, BiConsumer<Signal, SocketAddress> consumer) {

        this.consumers.get(signal).add(consumer);
    }

    @Override
    public void close() throws Exception {

        this.running.set(false);
        this.pool.shutdown();
        this.channel.close();

    }
}
