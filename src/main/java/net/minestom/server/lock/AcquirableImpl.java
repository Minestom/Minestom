package net.minestom.server.lock;

import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of {@link Acquirable}.
 * <p>
 * Class is immutable.
 *
 * @param <T> the object type which can be acquired
 */
public class AcquirableImpl<T> implements Acquirable<T> {

    private final T value;
    private final Handler handler;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
        this.handler = new Handler();
    }

    @NotNull
    @Override
    public T unwrap() {
        return value;
    }

    @NotNull
    @Override
    public Handler getHandler() {
        return handler;
    }
}