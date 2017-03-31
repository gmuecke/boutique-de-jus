package io.bdj.control;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Deque;
import java.util.concurrent.ScheduledExecutorService;

import io.bdj.util.process.ConsolePipe;
import io.bdj.util.signals.SignalTransceiver;

/**
 *
 */
public abstract class ProcessController {

    protected final InetAddress localhost = InetAddress.getLoopbackAddress();

    private SignalTransceiver com;
    private ConsolePipe console;
    private ScheduledExecutorService scheduler;
    private Deque<Process> processQueue;

    /**
     * Init method that is invoked during application initialization to share common facilities.
     * @param com
     *  the communication hub for sending and receiving signals from the managed processes
     * @param console
     * @param scheduler
     * @param processQueue
     */
    public void init(SignalTransceiver com,
                     ConsolePipe console,
                     final ScheduledExecutorService scheduler,
                     final Deque<Process> processQueue){
        this.com = com;
        this.console = console;
        this.scheduler = scheduler;
        this.processQueue = processQueue;
    }

    protected SignalTransceiver com() {
        return com;
    }

    protected ConsolePipe consolePipe() {

        return console;
    }

    protected ScheduledExecutorService getScheduler() {

        return scheduler;
    }

    protected Process startProcess(final String commandLine) {

        try {
            Process p = Runtime.getRuntime().exec(commandLine);
            this.processQueue.push(p);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
