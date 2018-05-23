package io.bdj.config;

import static java.util.logging.Logger.getLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central Configuration instance
 */
public final class Configuration {

    private static final Logger LOG = getLogger(Configuration.class.getName());

    /**
     * Central store for all configuration settings
     */
    private static final Map<String, String> CONFIG = new ConcurrentHashMap<>();
    private static final List<ConfigChangeListener> LISTENERS = new CopyOnWriteArrayList<>();
    /*
     We use a single thread. Every event may block the distribution of further events. This is intentional,
     that way event handling methods don't have to deal with concurrent events.
     */
    private static final ExecutorService EVENT_THREAD = Executors.newFixedThreadPool(1, new EventThreadFactory());

    static class EventThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        EventThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "config-event-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    private Configuration() {

    }

    public static void addListener(ConfigChangeListener listener) {

        LISTENERS.add(listener);
    }

    public static void setProperty(String key, String newValue) {

        CONFIG.put(key, newValue);
        notifyListeners(key, newValue);
    }

    private static void notifyListeners(final String key, final String newValue) {

        EVENT_THREAD.submit(() -> LISTENERS.stream().filter(l -> l.test(key)).forEach(l -> {
            try {
                l.accept(key, newValue);
            }catch(Exception e){
                LOG.log(Level.WARNING, "Applying config change " + key + "=" + newValue + " failed", e);
            }
        }));
    }

    public static Integer getInteger(String key) {

        return getInteger(key, 0);
    }

    public static Integer getInteger(String key, int defaultValue) {

        return getProperty(key).map(Integer::parseInt).orElse(defaultValue);
    }

    public static Optional<String> getProperty(String key) {

        String val = CONFIG.get(key);
        if (val == null) {
            val = System.getProperty(key);
        }

        return Optional.ofNullable(val);
    }

    public static Boolean getBoolean(String key) {

        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, boolean defaultValue) {

        return getProperty(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }
}
