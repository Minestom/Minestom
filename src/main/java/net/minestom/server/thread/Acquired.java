package net.minestom.server.thread;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an object that has been safely acquired and can be freed again.
 * <p>
 * This class should not be shared, and it is recommended to call {@link #unlock()}
 * once the acquisition goal has been fulfilled to limit blocking time.
 *
 * @param <T> the type of the acquired object
 */
public final class Acquired<T> {
    private final T value;
    private final Thread owner;
    private final ReentrantLock lock;
    private boolean unlocked;

    Acquired(T value, TickThread tickThread) {
        this.value = value;
        this.owner = Thread.currentThread();
        this.lock = AcquirableImpl.enter(owner, tickThread);
    }

    public @NotNull T get() {
        safeCheck();
        return value;
    }

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
