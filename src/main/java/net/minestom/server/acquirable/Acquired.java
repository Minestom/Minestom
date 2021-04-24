package net.minestom.server.acquirable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public class Acquired<T> {

    private final T value;

    private final boolean locked;
    private final ReentrantLock lock;

    protected Acquired(@NotNull T value,
                       boolean locked, @Nullable ReentrantLock lock) {
        this.value = value;
        this.locked = locked;
        this.lock = lock;
    }

    public @NotNull T get() {
        return value;
    }

    public void unlock() {
        if (!locked)
            return;
        Acquisition.acquireLeave(lock);
    }

}
