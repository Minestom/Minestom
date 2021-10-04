package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link Chunk chunks} and {@link Entity entities}.
 * <p>
 * Created in {@link ThreadDispatcher}, and awaken every tick with a task to execute.
 */
@ApiStatus.Internal
public final class TickThread extends MinestomThread {
    private final ReentrantLock lock = new ReentrantLock();
    private final Phaser phaser;
    private volatile boolean stop;

    private long tickTime;
    private final List<ThreadDispatcher.ChunkEntry> entries = new ArrayList<>();

    public TickThread(Phaser phaser, int number) {
        super(MinecraftServer.THREAD_NAME_TICK + "-" + number);
        this.phaser = phaser;
    }

    @Override
    public void run() {
        LockSupport.park(this);
        while (!stop) {
            this.lock.lock();
            try {
                tick();
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
            this.lock.unlock();
            // #acquire() callbacks
            this.phaser.arriveAndDeregister();
            LockSupport.park(this);
        }
    }

    private void tick() {
        for (ThreadDispatcher.ChunkEntry entry : entries) {
            final Chunk chunk = entry.chunk();
            try {
                chunk.tick(tickTime);
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
            final List<Entity> entities = entry.entities();
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    if (lock.hasQueuedThreads()) {
                        this.lock.unlock();
                        // #acquire() callbacks should be called here
                        this.lock.lock();
                    }
                    try {
                        entity.tick(tickTime);
                    } catch (Throwable e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                }
            }
        }
    }

    void startTick(long tickTime) {
        if (entries.isEmpty())
            return; // Nothing to tick
        this.phaser.register();
        this.tickTime = tickTime;
        this.stop = false;
        LockSupport.unpark(this);
    }

    public Collection<ThreadDispatcher.ChunkEntry> entries() {
        return entries;
    }

    /**
     * Gets the lock used to ensure the safety of entity acquisition.
     *
     * @return the thread lock
     */
    public @NotNull ReentrantLock lock() {
        return lock;
    }

    void shutdown() {
        this.stop = true;
        LockSupport.unpark(this);
    }
}
