package net.minestom.server.thread;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

final class AcquiredImpl<T> implements Acquired<T> {
    private final T value;
    private final Thread owner;
    private final ReentrantLock lock;
    private boolean unlocked;

    AcquiredImpl(T value, ReentrantLock lock) {
        this.value = value;
        this.owner = Thread.currentThread();
        this.lock = lock;
    }

    @Override
    public @NotNull T get() {
        safeCheck();
        return value;
    }

    @Override
    public void unlock() {
        safeCheck();
        this.unlocked = true;
        AcquirableImpl.leave(lock);
    }

    private void safeCheck() {
        Check.stateCondition(Thread.currentThread() != owner, "Acquired object is owned by the thread {0}", owner);
        Check.stateCondition(unlocked, "The acquired element has already been unlocked!");
    }
}
