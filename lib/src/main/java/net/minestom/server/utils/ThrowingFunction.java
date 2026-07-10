package net.minestom.server.utils;

import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface ThrowingFunction<I extends @UnknownNullability Object, O extends @UnknownNullability Object> {
    O apply(I i) throws Exception;

    static <T> ThrowingFunction<T, T> identity() {
        return t -> t;
    }
}
