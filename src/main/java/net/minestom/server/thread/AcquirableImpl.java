package net.minestom.server.thread;

import net.minestom.server.ServerFlag;
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
    private volatile @Nullable TickThread assignedThread;
    private volatile @Nullable TickOwner tickOwner;

    public AcquirableImpl(T value) {
        this.value = value;
    }

    @Override
    public Acquired<T> lock() {
        final TickOwner owner = this.tickOwner;
        if (owner != null) return new AcquiredImpl<>(unwrap(), enter(owner));
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
        final TickOwner owner = this.tickOwner;
        if (owner != null) return owner.isCurrentThread();
        final TickThread assignedThread = this.assignedThread;
        return Thread.currentThread() == Objects.requireNonNullElse(assignedThread, initThread);
    }

    @Override
    public boolean isOwned() {
        final TickOwner owner = this.tickOwner;
        if (owner != null) return isOwnedImpl(owner);
        final TickThread assignedThread = this.assignedThread;
        if (assignedThread == null) return Thread.currentThread() == initThread;
        return AcquirableImpl.isOwnedImpl(assignedThread);
    }

    @Override
    public void sync(Consumer<T> consumer) {
        final TickOwner owner = this.tickOwner;
        if (owner != null) {
            ReentrantLock lock = enter(owner);
            try {
                consumer.accept(unwrap());
            } finally {
                leave(lock);
            }
            return;
        }
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
    public boolean trySync(Consumer<T> consumer) {
        if (isOwned()) {
            consumer.accept(unwrap());
            return true;
        }
        final TickOwner owner = this.tickOwner;
        final TickThread assignedThread = this.assignedThread;
        final ReentrantLock lock = owner != null ? owner.lock()
                : assignedThread != null ? assignedThread.lock() : null;
        if (lock != null && lock.tryLock()) {
            try {
                consumer.accept(unwrap());
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public T unwrap() {
        return value;
    }

    @Override
    public @UnknownNullability TickThread assignedThread() {
        return assignedThread;
    }

    void assign(TickThread thread) {
        this.assignedThread = thread;
    }

    @Override
    public void assignOwner(@Nullable TickOwner owner) {
        this.tickOwner = owner;
    }

    @Override
    public void assertOwnership() {
        if (!ASSERTIONS_ENABLED && !ServerFlag.ACQUIRABLE_STRICT) return;
        if (isOwned()) return;
        TickOwner owner = this.tickOwner;
        TickThread assignedThread = this.assignedThread;
        Thread initThread = this.initThread;
        if (owner == null && assignedThread == null && Thread.currentThread() == initThread) return;
        throw new AcquirableOwnershipException(initThread, assignedThread, unwrap().toString());
    }

    void assertInitThread() {
        if (Thread.currentThread() != initThread)
            throw new IllegalStateException("Cannot lock an uninitialized Acquirable from a different thread");
    }

    static boolean isOwnedImpl(TickThread elementThread) {
        if (Thread.currentThread() == elementThread) return true;
        return elementThread.lock().isHeldByCurrentThread();
    }

    static boolean isOwnedImpl(TickOwner owner) {
        if (owner.isCurrentThread()) return true;
        return owner.lock().isHeldByCurrentThread();
    }

    static @Nullable ReentrantLock enter(TickThread elementThread) {
        if (isOwnedImpl(elementThread)) return null; // Nothing to lock, already owned by the current thread.
        return enterLock(elementThread.lock());
    }

    static @Nullable ReentrantLock enter(TickOwner owner) {
        if (isOwnedImpl(owner)) return null;
        return enterLock(owner.lock());
    }

    private static ReentrantLock enterLock(ReentrantLock targetLock) {
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
