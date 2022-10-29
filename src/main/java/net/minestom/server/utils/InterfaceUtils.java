package net.minestom.server.utils;

import java.util.function.BiConsumer;

public final class InterfaceUtils {
    private InterfaceUtils() {
        //no instance
    }

    public static <T, U> BiConsumer<T, U> flipBiConsumer(BiConsumer<U, T> biConsumer) {
        return (t, u) -> biConsumer.accept(u, t);
    }
}
