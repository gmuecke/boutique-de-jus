package io.bdj.config;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Listener that can be used to be notified on changed configuration values
 */
@FunctionalInterface
public interface ConfigChangeListener extends BiConsumer<String, String>, Predicate<String> {

    /**
     * Accepts changed configuration value
     * @param key
     *  the configuration key
     * @param newValue
     *  the new value of the changed configuration
     */
    @Override
    void accept(String key, String newValue);

    /**
     * Tests if the supplied key is accepted by this listener
     * @param key
     * @return
     */
    @Override
    default boolean test(String key){
        return true;
    }

    static ConfigChangeListener forConfigProperty(String key, BiConsumer<String,String> handler){

        Objects.requireNonNull(key, "Key must not be null");
        return new ConfigChangeListener() {
            @Override
            public void accept(final String key, String newValue) {
                handler.accept(key, newValue);
            }

            @Override
            public boolean test(final String testKey) {
                return key.equals(testKey);
            }
        };
    }
}
