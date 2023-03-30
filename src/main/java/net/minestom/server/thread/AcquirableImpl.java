package net.minestom.server.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

final class AcquirableImpl<T> implements Acquirable<T> {
    static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    /**
     * Global lock used for synchronization.
     */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private final T value;
    private TickThread assignedThread;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
    }

    @Override
    public @NotNull T unwrap() {
        return value;
    }

    @Override
    public @NotNull TickThread assignedThread() {
        VarHandle.acquireFence();
        return assignedThread;
    }

    void updateThread(@NotNull TickThread thread) {
        this.assignedThread = thread;
        VarHandle.releaseFence();
    }

    static @Nullable ReentrantLock enter(@NotNull Thread currentThread, @Nullable TickThread elementThread) {
        if (elementThread == null) return null;
        if (currentThread == elementThread) return null;
        final ReentrantLock currentLock = currentThread instanceof TickThread ? ((TickThread) currentThread).lock() : null;
        final ReentrantLock targetLock = elementThread.lock();
        if (targetLock.isHeldByCurrentThread()) return null;

        // Monitoring
        final long time = System.nanoTime();

        // Enter the target thread
        // TODO reduce global lock scope
        if (currentLock != null) {
            while (!GLOBAL_LOCK.tryLock()) {
                currentLock.unlock();
                currentLock.lock();
            }
        } else {
            GLOBAL_LOCK.lock();
        }
        targetLock.lock();

        // Monitoring
        WAIT_COUNTER_NANO.addAndGet(System.nanoTime() - time);
        return targetLock;
    }

    static void leave(@Nullable ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
            GLOBAL_LOCK.unlock();
        }
    }
}
