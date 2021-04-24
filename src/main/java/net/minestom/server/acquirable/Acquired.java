package net.minestom.server.acquirable;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public class Acquired<T> {

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
        Acquisition.acquireLeave(lock);
    }

    private void checkLock() {
        Check.stateCondition(unlocked, "The acquired element has already been unlocked!");
    }

}
