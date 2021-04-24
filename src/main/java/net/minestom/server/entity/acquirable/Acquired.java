package net.minestom.server.entity.acquirable;

import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class Acquired<T> {

    private final T value;
    private final TickThread tickThread;

    protected Acquired(@NotNull T value, @NotNull TickThread tickThread) {
        this.value = value;
        this.tickThread = tickThread;
    }

    public void sync(@NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();
        Acquisition.acquire(currentThread, tickThread, () -> consumer.accept(unwrap()));
    }

    public void async(@NotNull Consumer<T> consumer) {
        // TODO per-thread list
        AsyncUtils.runAsync(() -> sync(consumer));
    }

    public @NotNull Optional<T> optional() {
        final Thread currentThread = Thread.currentThread();
        if (Objects.equals(currentThread, tickThread)) {
            return Optional.of(unwrap());
        }
        return Optional.empty();
    }

    public @NotNull T unwrap() {
        return value;
    }
}
