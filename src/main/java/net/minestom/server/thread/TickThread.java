package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link net.minestom.server.instance.Chunk chunks} and {@link net.minestom.server.entity.Entity entities}.
 * <p>
 * Created in {@link ThreadDispatcher}, and awaken every tick with a task to execute.
 */
public final class TickThread extends Thread {
    private final ReentrantLock lock = new ReentrantLock();
    private final Phaser phaser;
    private volatile boolean stop;
    private Runnable tickRunnable;

    private boolean isTicking;
    private Collection<ThreadDispatcher.ChunkEntry> entries;

    public TickThread(Phaser phaser, int number) {
        super(MinecraftServer.THREAD_NAME_TICK + "-" + number);
        this.phaser = phaser;
    }

    @Override
    public void run() {
        LockSupport.park(this);
        while (!stop) {
            this.isTicking = true;
            this.tickRunnable.run();
            this.isTicking = false;
            this.phaser.arriveAndDeregister();
            LockSupport.park(this);
        }
    }

    void startTick(@NotNull Runnable runnable) {
        this.tickRunnable = runnable;
        LockSupport.unpark(this);
    }

    public Collection<ThreadDispatcher.ChunkEntry> entries() {
        return isTicking ? entries : Collections.emptyList();
    }

    void updateEntries(Collection<ThreadDispatcher.ChunkEntry> entries) {
        this.entries = entries;
    }

    /**
     * Gets the lock used to ensure the safety of entity acquisition.
     *
     * @return the thread lock
     */
    public @NotNull ReentrantLock lock() {
        return lock;
    }

    /**
     * Shutdowns the thread. Cannot be undone.
     */
    public void shutdown() {
        this.stop = true;
        LockSupport.unpark(this);
    }
}
