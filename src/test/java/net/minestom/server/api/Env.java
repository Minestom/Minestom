package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public interface Env {
    @NotNull ServerProcess process();

    @NotNull TestConnection createConnection();

    default void tick() {
        process().ticker().tick(System.nanoTime());
    }

    default boolean tickWhile(BooleanSupplier condition, Duration timeout) {
        var ticker = process().ticker();
        final long start = System.nanoTime();
        while (condition.getAsBoolean()) {
            final long tick = System.nanoTime();
            ticker.tick(tick);
            if (timeout != null && System.nanoTime() - start > timeout.toNanos()) {
                return false;
            }
        }
        return true;
    }
}
