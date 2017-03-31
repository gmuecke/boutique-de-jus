package io.bdj.util.process;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bdj.util.threads.NamedThreadFactory;

/**
 *
 */
public class ConsolePipe implements AutoCloseable {

    private static final Logger LOG = getLogger(ConsolePipe.class.getName());

    private static final AtomicInteger SCHEDULER_THREADS = new AtomicInteger(1);

    //a single pool for all registered processes should be ok?
    private final ScheduledExecutorService pool;

    private final Map<Process, PipeTuple> errStreams;
    private final Map<Process, PipeTuple> outStreams;


    public ConsolePipe() {

        this.pool = Executors.newScheduledThreadPool(1,
                                                     new NamedThreadFactory(() -> "console-pipe-"
                                                             + SCHEDULER_THREADS.getAndIncrement()));
        this.errStreams = new ConcurrentHashMap<>();
        this.outStreams = new ConcurrentHashMap<>();

    }

    public ConsolePipe open() {

        if (this.pool.isShutdown()) {
            throw new IllegalStateException("ConsolePipe already closed");
        }
        this.pool.scheduleAtFixedRate(() -> errStreams.values().forEach(this::forward), 0, 100, TimeUnit.MILLISECONDS);
        this.pool.scheduleAtFixedRate(() -> outStreams.values().forEach(this::forward), 0, 250, TimeUnit.MILLISECONDS);
        this.pool.scheduleAtFixedRate(() ->  {
            purgeTerminatedProcesses(errStreams);
            purgeTerminatedProcesses(outStreams);
        }, 1, 1, TimeUnit.SECONDS);
        return this;
    }

    private void purgeTerminatedProcesses(final Map<Process, PipeTuple> streams) {

        streams.keySet().stream().filter(p -> !p.isAlive()).collect(toList()).forEach(streams::remove);
    }

    private void forward(final PipeTuple pipe) {

        int bytesRead;
        try {
            while (pipe.in.available() > 0) {
                byte[] buf = new byte[pipe.in.available()];
                bytesRead = pipe.in.read(buf);
                pipe.out.accept(ByteBuffer.wrap(buf, 0, bytesRead));
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, e, () -> "Could not read stream");
        }
    }

    /**
     * Registers a process to forward the out to System.out and System.err
     *
     * @param p
     *         the process to register
     *
     * @return this forward
     */
    public ConsolePipe register(Process p) {

        return register(p, System.err, System.out);
    }

    /**
     * Registers a process to forward the output to one of the specified streams
     *
     * @param p
     *         the process to register
     * @param errStream
     *         the error stream to write error output to
     * @param outStream
     *         the output stream to write standard output to
     *
     * @return this forward
     */
    public ConsolePipe register(Process p, PrintStream errStream, PrintStream outStream) {

        return register(p,
                        b -> errStream.print(b.asCharBuffer().toString()),
                        b -> outStream.print(b.asCharBuffer().toString()));
    }

    /**
     * Registers a process to forward the output to one of the specified streams
     *
     * @param p
     *         the process to register
     * @param errData
     *         consumer for data received from the error stream
     * @param outData
     *         consumer for data received from the standard output stream
     *
     * @return this forward
     */
    public ConsolePipe register(Process p, Consumer<ByteBuffer> errData, Consumer<ByteBuffer> outData) {

        this.errStreams.put(p, new PipeTuple(p.getErrorStream(), errData));
        this.outStreams.put(p, new PipeTuple(p.getInputStream(), outData));

        return this;
    }

    @Override
    public void close() throws Exception {

        this.pool.shutdown();
    }

    private static class PipeTuple {

        final InputStream in;
        final Consumer<ByteBuffer> out;

        public PipeTuple(final InputStream in, final Consumer<ByteBuffer> out) {

            this.in = in;
            this.out = out;
        }
    }

}
