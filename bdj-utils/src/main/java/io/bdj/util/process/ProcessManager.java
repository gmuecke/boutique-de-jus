package io.bdj.util.process;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 *
 */
public class ProcessManager implements AutoCloseable {

    private final ScheduledExecutorService scheduler;
    private final Deque<Process> processQueue;
    private final Map<Process, List<Consumer<Process>>> processEndListeners;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Thread shutdownHook;


    public ProcessManager() {

        this(Executors.newScheduledThreadPool(1));

    }

    public ProcessManager(final ScheduledExecutorService scheduler) {

        this.scheduler = scheduler;
        this.processEndListeners = new ConcurrentHashMap<>();
        this.processQueue = new ConcurrentLinkedDeque<>();
        this.shutdownHook = new Thread(this::destroyProcesses);

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

    @Override
    public void close() throws Exception {
        this.scheduler.shutdown();
        destroyProcesses();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    private void handleTerminatedProcess(final Process process) {

        this.processQueue.remove(process);
        if (this.processEndListeners.containsKey(process)) {
            this.processEndListeners.get(process).forEach(c -> c.accept(process));
            this.processEndListeners.remove(process);
        }
    }

    public Process startProcess(final String commandLine) {

        return startProcess(commandLine, null, null);
    }

    public Process startProcess(final String commandLine, String[] env, File workDir) {

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

    private void destroyProcesses(){
        this.processQueue.forEach(Process::destroy);
    }
}
