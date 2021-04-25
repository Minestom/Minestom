package net.minestom.server.acquirable;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

class AcquirableImpl<T> implements Acquirable<T> {

    protected static final ThreadLocal<Stream<Entity>> CURRENT_ENTITIES = ThreadLocal.withInitial(Stream::empty);
    protected static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    private final T value;
    private final Acquirable.Handler handler;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
        this.handler = new Acquirable.Handler();
    }

    @Override
    public @NotNull T unwrap() {
        return value;
    }

    @Override
    public @NotNull Acquirable.Handler getHandler() {
        return handler;
    }
}
