package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;

public interface Point {
    @Contract(pure = true)
    double x();

    @Contract(pure = true)
    double y();

    @Contract(pure = true)
    double z();
}
