package net.minestom.server.acquirable;

import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.thread.TickThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

class AcquirableImpl<T> implements Acquirable<T> {

    protected static final ThreadLocal<Collection<ThreadProvider.ChunkEntry>> ENTRIES = ThreadLocal.withInitial(Collections::emptySet);
    protected static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

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

    protected static @Nullable ReentrantLock enter(@Nullable Thread currentThread, @Nullable TickThread elementThread) {
        // Monitoring
        long time = System.nanoTime();

        ReentrantLock currentLock;
        {
            final TickThread current = currentThread instanceof TickThread ?
                    (TickThread) currentThread : null;
            currentLock = current != null && current.getLock().isHeldByCurrentThread() ?
                    current.getLock() : null;
        }
        if (currentLock != null)
            currentLock.unlock();

        GLOBAL_LOCK.lock();

        if (currentLock != null)
            currentLock.lock();

        final var lock = elementThread != null ? elementThread.getLock() : null;
        final boolean acquired = lock == null || lock.isHeldByCurrentThread();
        if (!acquired) {
            lock.lock();
        }

        // Monitoring
        AcquirableImpl.WAIT_COUNTER_NANO.addAndGet(System.nanoTime() - time);

        return !acquired ? lock : null;
    }

    protected static void leave(@Nullable ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
        }
        GLOBAL_LOCK.unlock();
    }
}
