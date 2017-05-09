package io.bdj.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central Configuration instance
 */
public final class Configuration {

    /**
     * Central store for all configuration settings
     */
    private static final Map<String, String> CONFIG = new ConcurrentHashMap<>();
    private static final List<ConfigChangeListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final ExecutorService EVENT_THREAD = Executors.newFixedThreadPool(1);

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
        EVENT_THREAD.execute(() -> LISTENERS.stream().filter(l -> l.test(key)).forEach(l -> l.accept(key, newValue)));
    }

    public static Integer getInteger(String key) {

        return getInteger(key, 0);
    }

    public static Integer getInteger(String key, int defaultValue) {

        return getProperty(key).map(Integer::parseInt).orElse(defaultValue);
    }

    public static Optional<String> getProperty(String key) {

        return Optional.ofNullable(CONFIG.get(key));
    }

    public static Boolean getBoolean(String key) {

        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, boolean defaultValue) {

        return getProperty(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }
}
