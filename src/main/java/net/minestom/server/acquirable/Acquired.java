package net.minestom.server.acquirable;

import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

public final class Acquired<T> {
    private final T value;
    private final ReentrantLock lock;
    private boolean unlocked;

    Acquired(T value, TickThread tickThread) {
        this.value = value;
        this.lock = AcquirableImpl.enter(Thread.currentThread(), tickThread);
    }

    public @NotNull T get() {
        checkLock();
        return value;
    }

    public void unlock() {
        checkLock();
        this.unlocked = true;
        AcquirableImpl.leave(lock);
    }

    private void checkLock() {
        Check.stateCondition(unlocked, "The acquired element has already been unlocked!");
    }
}
