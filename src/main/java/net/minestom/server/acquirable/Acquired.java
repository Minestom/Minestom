package net.minestom.server.acquirable;

import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

public final class Acquired<T> {
    private final T value;

    private final boolean locked;
    private final ReentrantLock lock;

    private boolean unlocked;

    static <T> Acquired<T> local(@NotNull T value) {
        return new Acquired<>(value, false, null, null);
    }

    static <T> Acquired<T> locked(@NotNull Acquirable<T> acquirable) {
        final Thread currentThread = Thread.currentThread();
        final TickThread tickThread = acquirable.getHandler().getTickThread();
        return new Acquired<>(acquirable.unwrap(), true, currentThread, tickThread);
    }

    private Acquired(@NotNull T value,
                     boolean locked, Thread currentThread, TickThread tickThread) {
        this.value = value;
        this.locked = locked;
        this.lock = locked ? AcquirableImpl.enter(currentThread, tickThread) : null;
    }

    public @NotNull T get() {
        checkLock();
        return value;
    }

    public void unlock() {
        checkLock();
        this.unlocked = true;
        if (!locked) return;
        AcquirableImpl.leave(lock);
    }

    private void checkLock() {
        Check.stateCondition(unlocked, "The acquired element has already been unlocked!");
    }
}
