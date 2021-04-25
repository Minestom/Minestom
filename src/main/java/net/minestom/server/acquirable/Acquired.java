package net.minestom.server.acquirable;

import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public class Acquired<T> {

    /**
     * Global lock used for synchronization.
     */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private final T value;

    private final boolean locked;
    private final ReentrantLock lock;

    private boolean unlocked;

    protected Acquired(@NotNull T value,
                       boolean locked, @Nullable ReentrantLock lock) {
        this.value = value;
        this.locked = locked;
        this.lock = lock;
    }

    public @NotNull T get() {
        checkLock();
        return value;
    }

    public void unlock() {
        checkLock();
        this.unlocked = true;
        if (!locked)
            return;
        acquireLeave(lock);
    }

    protected static @Nullable ReentrantLock acquireEnter(@Nullable Thread currentThread, @Nullable TickThread elementThread) {
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

    protected static void acquireLeave(@Nullable ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
        }
        GLOBAL_LOCK.unlock();
    }

    private void checkLock() {
        Check.stateCondition(unlocked, "The acquired element has already been unlocked!");
    }

}
