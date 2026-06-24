package net.minestom.server.thread;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

final class AcquiredImpl<T> implements Acquired<T> {
    private final T value;
    private final Thread owner;
    private final @Nullable ReentrantLock lock;
    private boolean unlocked;

    AcquiredImpl(T value, @Nullable ReentrantLock lock) {
        this.value = value;
        this.owner = Thread.currentThread();
        this.lock = lock;
    }

    @Override
    public T get() {
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
        if (Thread.currentThread() != owner)
            throw new IllegalStateException("Acquired object is owned by the thread " + owner);
        if (unlocked)
            throw new IllegalStateException("The acquired element has already been unlocked!");
    }
}
