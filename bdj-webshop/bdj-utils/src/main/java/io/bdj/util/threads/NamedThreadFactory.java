package io.bdj.util.threads;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

/**
 *
 */
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final Supplier<String> nameSupplier;
    private final boolean demonThreads;
    private final int priority;

    public NamedThreadFactory(Supplier<String> name){
        this(name, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(Supplier<String> name, boolean deamonThreads, int priority){

        this.nameSupplier = name;
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.demonThreads = deamonThreads;
        this.priority = priority;
    }


    @Override
    public Thread newThread(final Runnable r) {
        Thread t = new Thread(group, r, nameSupplier.get(), 0);
        t.setDaemon(this.demonThreads);
        t.setPriority(this.priority);
        return t;
    }
}
