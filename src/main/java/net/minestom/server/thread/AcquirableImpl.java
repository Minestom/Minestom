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
    static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    /**
     * Lock used specifically for uninitialized elements (null thread).
     */
    static final ReentrantLock UNINITIALIZED_LOCK = new ReentrantLock();

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

    static boolean isOwnedImpl(@Nullable TickThread elementThread) {
        if (Thread.currentThread() == elementThread) return true; // Element is from the current thread.
        if (elementThread == null) {
            // Uninitialized entity, must check the dedicated lock
            return UNINITIALIZED_LOCK.isHeldByCurrentThread();
        }
        return elementThread.lock().isHeldByCurrentThread();
    }

    static @Nullable ReentrantLock enter(@Nullable TickThread elementThread) {
        if (isOwnedImpl(elementThread)) return null; // Nothing to lock, already owned by the current thread.

        // Monitoring
        final long time = System.nanoTime();

        if (elementThread == null) {
            // Element isn't initialized yet, use uninitialized lock
            UNINITIALIZED_LOCK.lock();
            WAIT_COUNTER_NANO.addAndGet(System.nanoTime() - time);
            return UNINITIALIZED_LOCK;
        }

        final ReentrantLock targetLock = elementThread.lock();
        final ReentrantLock currentLock = Thread.currentThread() instanceof TickThread tickThread ? tickThread.lock() : null;
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
        if (lock == UNINITIALIZED_LOCK) {
            UNINITIALIZED_LOCK.unlock();
        } else {
            if (lock != null) {
                lock.unlock();
                GLOBAL_LOCK.unlock();
            }
        }
    }
}
