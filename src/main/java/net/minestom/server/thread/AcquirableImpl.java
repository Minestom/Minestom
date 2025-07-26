package net.minestom.server.thread;

import net.minestom.server.ServerFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

final class AcquirableImpl<T> implements Acquirable<T> {
    private static final boolean ASSERTIONS_ENABLED = AcquirableImpl.class.desiredAssertionStatus();
    static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    /**
     * Global lock used for synchronization.
     */
    static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private final T value;
    private final Thread initThread = Thread.currentThread();
    private volatile TickThread assignedThread;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
    }

    @Override
    public @NotNull Acquired<T> lock() {
        final TickThread assignedThread = this.assignedThread;
        if (assignedThread == null) {
            assertInitThread();
            return new AcquiredImpl<>(unwrap(), null);
        }
        ReentrantLock lock = enter(assignedThread);
        assert assignedThread.lock().isHeldByCurrentThread();
        return new AcquiredImpl<>(unwrap(), lock);
    }

    @Override
    public boolean isLocal() {
        final TickThread assignedThread = this.assignedThread;
        return Thread.currentThread() == Objects.requireNonNullElse(assignedThread, initThread);
    }

    @Override
    public boolean isOwned() {
        final TickThread assignedThread = this.assignedThread;
        if (assignedThread == null) return Thread.currentThread() == initThread;
        return AcquirableImpl.isOwnedImpl(assignedThread);
    }

    @Override
    public void sync(@NotNull Consumer<T> consumer) {
        final TickThread assignedThread = this.assignedThread;
        if (assignedThread == null) {
            assertInitThread();
            consumer.accept(unwrap());
            return;
        }
        ReentrantLock lock = enter(assignedThread);
        try {
            assert assignedThread.lock().isHeldByCurrentThread();
            consumer.accept(unwrap());
        } finally {
            leave(lock);
        }
    }

    @Override
    public boolean trySync(@NotNull Consumer<T> consumer) {
        if (isOwned()) {
            consumer.accept(unwrap());
            return true;
        }
        TickThread assignedThread = this.assignedThread;
        if (assignedThread != null) {
            ReentrantLock lock = assignedThread.lock();
            if (lock.tryLock()) {
                try {
                    consumer.accept(unwrap());
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull T unwrap() {
        return value;
    }

    @Override
    public @UnknownNullability TickThread assignedThread() {
        return assignedThread;
    }

    void assign(@NotNull TickThread thread) {
        this.assignedThread = thread;
    }

    @Override
    public void assertOwnership() {
        if (!ASSERTIONS_ENABLED && !ServerFlag.ACQUIRABLE_STRICT) return;
        if (isOwned()) return;
        Thread currentThread = Thread.currentThread();
        Thread initThread = this.initThread;
        TickThread assignedThread = this.assignedThread;
        if (assignedThread == null && currentThread == initThread) return;
        throw new AcquirableOwnershipException(currentThread, initThread, assignedThread, unwrap().toString());
    }

    void assertInitThread() {
        if (Thread.currentThread() != initThread)
            throw new IllegalStateException("Cannot lock an uninitialized Acquirable from a different thread");
    }

    static boolean isOwnedImpl(@NotNull TickThread elementThread) {
        if (Thread.currentThread() == elementThread) return true;
        return elementThread.lock().isHeldByCurrentThread();
    }

    static @Nullable ReentrantLock enter(@NotNull TickThread elementThread) {
        if (isOwnedImpl(elementThread)) return null; // Nothing to lock, already owned by the current thread.
        final long time = System.nanoTime();
        // Enter the target thread
        if (Thread.currentThread() instanceof TickThread tickThread && tickThread.lock().isHeldByCurrentThread()) {
            while (!GLOBAL_LOCK.tryLock()) {
                tickThread.lock().unlock();
                tickThread.lock().lock();
            }
        } else {
            GLOBAL_LOCK.lock();
        }
        final ReentrantLock targetLock = elementThread.lock();
        targetLock.lock();
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
