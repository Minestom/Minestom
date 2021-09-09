package net.minestom.server.acquirable;

import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.thread.TickThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

final class AcquirableImpl<T> implements Acquirable<T> {
    static final ThreadLocal<Collection<ThreadProvider.ChunkEntry>> ENTRIES = ThreadLocal.withInitial(Collections::emptySet);
    static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    /**
     * Global lock used for synchronization.
     */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

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

    static @Nullable ReentrantLock enter(@Nullable Thread currentThread, @Nullable TickThread elementThread) {
        // Monitoring
        final long time = System.nanoTime();

        ReentrantLock currentLock;
        {
            final ReentrantLock lock = currentThread instanceof TickThread ?
                    ((TickThread) currentThread).getLock() : null;
            currentLock = lock != null && lock.isHeldByCurrentThread() ? lock : null;
        }
        if (currentLock != null) currentLock.unlock();
        GLOBAL_LOCK.lock();
        if (currentLock != null) currentLock.lock();

        final ReentrantLock lock = elementThread != null ? elementThread.getLock() : null;
        final boolean acquired = lock == null || lock.isHeldByCurrentThread();
        if (!acquired) lock.lock();

        // Monitoring
        AcquirableImpl.WAIT_COUNTER_NANO.addAndGet(System.nanoTime() - time);

        return !acquired ? lock : null;
    }

    static void leave(@Nullable ReentrantLock lock) {
        if (lock != null) lock.unlock();
        GLOBAL_LOCK.unlock();
    }
}
