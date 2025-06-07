package net.minestom.server.utils;

@FunctionalInterface
public interface ThrowingFunction<I, O> {
    O apply(I i) throws Exception;

    static <T> ThrowingFunction<T, T> identity() {
        return t -> t;
    }
}
