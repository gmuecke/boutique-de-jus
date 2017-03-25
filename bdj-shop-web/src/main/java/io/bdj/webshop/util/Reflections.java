package io.bdj.webshop.util;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * Utility class for dealing with reflections
 */
public final class Reflections {

    private Reflections(){}

    /**
     * Creates a partial function for looking up an invoking a specific method on a subject.
     * The method must be accessible (i.e. public)
     * @param methodName
     *  the name of the method
     * @param <IN>
     *     the input type of the subject on which the method should be applied
     * @param <OUT>
     *     the return type of the method invocation
     * @return
     *  a function encapsulating the method lookup&invocation
     */
    public static <IN, OUT> Function<IN, OUT> invokeMethod(String methodName) {

        return in -> {
            try {
                return (OUT) in.getClass().getMethod(methodName).invoke(in);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
