package io.bdj.util.signals;

import static java.util.function.Function.identity;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SignalTransceiver implements AutoCloseable {

    private static final Logger LOG = getLogger(SignalTransceiver.class.getName());
    private static final AtomicInteger SCHEDULER_THREADS = new AtomicInteger(1);

    private final int listenPort;
    private final InetAddress address;

    private final Thread receiverThread;
    private final Map<Signal, List<Consumer<Event>>> consumers;

    private final DatagramChannel receiverChannel;
    private final DatagramChannel sendChannel;
    private AtomicBoolean running = new AtomicBoolean(false);

    private SignalTransceiver(InetAddress thisAddr, final int listenPort) {

        this.listenPort = listenPort;
        this.address = thisAddr;
        InetSocketAddress addr = new InetSocketAddress(thisAddr, listenPort);
        try {
            this.sendChannel = DatagramChannel.open();
            this.receiverChannel = DatagramChannel.open();
            //TODO support blocking/non-block (non-blocking-scheduled?)
            this.receiverChannel.configureBlocking(true);

            this.receiverChannel.bind(addr);
        } catch (BindException e){
            throw new RuntimeException("Address " + addr + " already bound", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not open datagram channel", e);
        }
        //create an list of consumers for for each signal
        this.consumers = Arrays.stream(Signal.values()).collect(toMap(identity(), s -> new CopyOnWriteArrayList<>()));

        final ByteBuffer buffer = ByteBuffer.allocate(1536);
        this.receiverThread = new Thread(() -> {
            while (running.get()) {
                try {
                    if (this.receiverChannel.receive(buffer) != null && buffer.position() > 0) {
                        buffer.flip();
                        Optional.ofNullable(Event.fromBuffer(buffer)).ifPresent(event -> {
                            LOG.finer(() -> "Received " + event);
                            this.consumers.get(event.getSignal()).forEach(c -> c.accept(event));
                        });
                        buffer.clear();
                    }
                } catch(AsynchronousCloseException e){
                    LOG.log(Level.INFO, () -> "Channel closed");
                    running.set(false);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, e, () -> "Could not receive package");
                }
            }

        }, "signal-receiver-" + SCHEDULER_THREADS.getAndIncrement());

    }

    public static SignalTransceiver create(int listenPort) {

        return create(InetAddress.getLoopbackAddress(), listenPort);
    }

    public static SignalTransceiver create(InetAddress address, int listenPort) {

        return new SignalTransceiver(address, listenPort);
    }

    public static void acceptAndWait(int listenPort, BiConsumer<SignalTransceiver, CompletableFuture<Event>> prepare){
        acceptAndWait(InetAddress.getLoopbackAddress(), listenPort, prepare);
    }

    /**
     * Creates a transceiver and waits for receiving a stop signal. The method blocks until the future passed
     * to the preparation consumer is completed. It's up to the consumer on which event the transceiver should stop
     * @param address
     *  the address to listen for packets
     * @param listenPort
     *  the port to listen for packets (UDP)
     * @param prepare
     *  a consumer that gets the instance of the transceiver in order to setup proper actions on receiving events
     */
    public static void acceptAndWait(InetAddress address, int listenPort, BiConsumer<SignalTransceiver, CompletableFuture<Event>> prepare){
        try (SignalTransceiver com = SignalTransceiver.create(address, listenPort).start()) {
            CompletableFuture<Event> stopListening = new CompletableFuture<>();
            prepare.accept(com, stopListening);
            while (!stopListening.isDone() || stopListening.isCancelled()) {
                Thread.sleep(150);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failure to receive signals", e);
        }
    }

    public boolean send(Signal signal, SocketAddress dst) {

        return send(Event.from(signal), dst);
    }

    public boolean send(final Event event, SocketAddress dst) {

        try {
            final Event evt;
            if (event.hasSource()) {
                evt = event;
            } else {
                evt = Event.from(event, this.address, this.listenPort);
            }
            LOG.finer(() -> "Sending " + evt + " to " + dst);
            this.sendChannel.send(ByteBuffer.wrap(evt.toBytes()), dst);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, e, () -> "Could not send signal");
            return false;
        }
    }

    public boolean send(Signal signal, byte[] payload, SocketAddress dst) {

        return send(Event.from(signal, payload), dst);
    }

    public boolean send(Signal signal, String payload, SocketAddress dst) {

        return send(Event.from(signal, payload.getBytes()), dst);
    }

    public SignalTransceiver start() {

        if (!this.running.compareAndSet(false, true)) {
            throw new IllegalStateException("Receiver already started");
        }
        LOG.info(() -> "Signal Receiver started, listening on " + this.address + ":" + this.listenPort);
        this.receiverThread.start();
        return this;
    }

    public SignalTransceiver onReceive(Signal signal, Consumer<Event> consumer) {
        this.consumers.get(signal).add(consumer);
        return this;
    }

    @Override
    public void close() throws Exception {

        this.running.set(false);
        try (DatagramChannel ch1 = this.receiverChannel;
             DatagramChannel ch2 = this.sendChannel) {
        } catch (Exception e) {
            //ignore
        }
    }
}
