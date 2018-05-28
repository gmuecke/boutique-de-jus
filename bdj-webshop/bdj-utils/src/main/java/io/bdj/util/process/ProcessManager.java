package io.bdj.util.process;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;

/**
 *
 */
public class ProcessManager implements AutoCloseable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProcessManager.class);

    private final ScheduledExecutorService scheduler;
    private final Deque<Process> processQueue;
    private final Map<Process, List<Consumer<Process>>> processEndListeners;
    private final Map<Process, List<ScheduledFuture>> observers;
    private final Map<SocketAddress, Consumer<Signal>> monitorEventHandlers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Thread shutdownHook;
    private final SignalTransceiver com;

    public ProcessManager(final ScheduledExecutorService scheduler, SignalTransceiver com) {

        this.scheduler = scheduler;
        this.com = com;
        this.processEndListeners = new ConcurrentHashMap<>();
        this.processQueue = new ConcurrentLinkedDeque<>();
        this.shutdownHook = new Thread(this::destroyProcesses);
        this.observers = new ConcurrentHashMap<>();
        this.monitorEventHandlers = new ConcurrentHashMap<>();

    }

    public ProcessManager start() {

        if (!this.running.compareAndSet(false, true)) {
            throw new IllegalStateException("Process Manager already started");
        }

        //purge terminated processes
        this.scheduler.scheduleAtFixedRate(() -> processQueue.stream()
                                                             .filter(p -> !p.isAlive())
                                                             .collect(toList())
                                                             .forEach(process -> {
                                                                 handleTerminatedProcess(process);
                                                             }), 1, 1, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return this;
    }

    private void handleTerminatedProcess(final Process process) {

        this.processQueue.remove(process);
        if (this.processEndListeners.containsKey(process)) {
            this.processEndListeners.get(process).forEach(c -> c.accept(process));
        }
        this.processEndListeners.remove(process);
        if(this.observers.containsKey(process)){
            this.observers.get(process).forEach(c -> c.cancel(true));
        }
        this.observers.remove(process);
    }

    @Override
    public void close() throws Exception {

        this.scheduler.shutdown();
        destroyProcesses();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    private void destroyProcesses() {

        this.processQueue.forEach(Process::destroy);
    }

    public Process startProcess(final String commandLine) {

        return startProcess(commandLine, null, null);
    }

    public Process startProcess(final String commandLine, String[] env, File workDir) {

        LOG.info("Spawning new process using command line: {}", commandLine);

        if (!this.running.get()) {
            throw new IllegalStateException("Process Manager not running");
        }
        try {
            Process p = Runtime.getRuntime().exec(commandLine);
            this.processQueue.push(p);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addProcessEndListener(Process process, Consumer<Process> listener) {

        if (!this.processQueue.contains(process)) {
            throw new IllegalStateException("Process " + process + " is not managed or already terminated");
        }
        this.processEndListeners.putIfAbsent(process, new CopyOnWriteArrayList<>());
        this.processEndListeners.get(process).add(listener);
    }

    public void observe(final Process process,
                        final SocketAddress serviceAddress,
                        final Consumer<Signal> signalHandler) {

        this.monitorEventHandlers.put(serviceAddress, signalHandler);

        com.onReceive(Signal.STATUS_OK, e -> {
            if (this.monitorEventHandlers.containsKey(e.getReplyAddr())) {
                this.monitorEventHandlers.get(e.getReplyAddr()).accept(e.getSignal());
            }
        });

        this.observers.putIfAbsent(process, new CopyOnWriteArrayList<>());
        //query the status every 1 second
        this.observers.get(process)
                      .add(this.scheduler.scheduleAtFixedRate(() -> com.send(Signal.QUERY_STATUS, serviceAddress),
                                                              1,
                                                              1,
                                                              TimeUnit.SECONDS));
    }
}
