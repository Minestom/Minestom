package net.minestom.server.entity;

import it.unimi.dsi.fastutil.longs.*;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.ChunkAndClaim;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import net.minestom.server.instance.chunksystem.ClaimCallbacks;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsible for loading chunks around and sending chunks to the player
 */
public class PlayerChunkTracker {
    private static final ChunkClaim.Shape CLAIM_SHAPE = ServerFlag.INSIDE_TEST ? ChunkClaim.Shape.SQUARE : ChunkClaim.Shape.CIRCLE;
    private final Player player;
    // Even though adding/removing claims is done on the player thread, we need this lock for the callbacks and updating visibleChunks
    private final ReentrantLock lock = new ReentrantLock();
    private final LongSet chunksSentOrInQueue = new LongOpenHashSet();
    private @Nullable Tracked tracked;
    private boolean spawningPlayer;
    private volatile int priority = 0;
    // TODO remove, was used for debugging purposes. Maybe this can also be modified to an assert statement?
    private final AtomicInteger staleCallbackCount = new AtomicInteger();

    public PlayerChunkTracker(Player player) {
        this.player = player;
    }

    public void changePosition() {
        changePosition(true);
    }

    public void changePosition(boolean sendUnloads) {
        var instance = player.getInstance();
        if (instance == null) throw new IllegalStateException("No instance set");
        var position = player.getPosition();
        var tracked = addClaim(instance, position);
        changeTracked(tracked, sendUnloads);
    }

    /**
     * Changes the tracked chunk. (Basically moves a claim).
     * Only called if old and new claims are on the same chunk manager.
     * <p>
     * Always called from player tick thread.
     *
     * @param tracked     the new chunk to track
     * @param sendUnloads whether unload packets should be sent
     */
    public void changeTracked(Tracked tracked, boolean sendUnloads) {
        lock.lock();
        try {
            if (this.tracked == null) {
                throw new IllegalStateException("Tried to change tracking when nothing was being tracked. This is most likely a logic error.");
            }
            if (tracked.chunkManager() != this.tracked.chunkManager()) {
                throw new IllegalArgumentException("Tried to change tracking to a tracked object from another instance. This is not allowed!");
            }

            this.tracked.untrack();

            // The simplest approach is to iterate through all chunks. Let's use that and see how well it does
            for (var it = chunksSentOrInQueue.longIterator(); it.hasNext(); ) {
                var chunkIndex = it.nextLong();
                var chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                var chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                if (tracked.chunkAndClaim().claim().contains(chunkX, chunkZ)) {
                    // Chunk is visible in the new claim
                    continue;
                }
                it.remove();
                player.getChunkQueue().cancelSend(chunkX, chunkZ);
                if (sendUnloads) {
                    // TODO there may be an argument to have an "unload queue"
                    unloadChunk(chunkX, chunkZ);
                }
            }

            this.tracked = tracked;
            sendStale();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Special method to update the instance of the tracked object, but don't send any chunk updates.
     * Used for shared instances
     */
    public void updateInstanceSameChunks(Tracked tracked) {
        lock.lock();
        try {
            if (this.tracked == null) {
                throw new IllegalStateException("Tried to change tracking when nothing was being tracked. This is most likely a logic error.");
            }
            this.tracked.untrack();
            this.tracked = tracked;
        } finally {
            lock.unlock();
        }
    }

    public void beginRespawn() {
        lock.lock();
        try {
            player.getChunkQueue().resetState();
            chunksSentOrInQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    public void finishRespawn() {

    }

    @ApiStatus.Internal
    public void beginSpawnPlayer() {
        lock.lock();
        try {
            if (spawningPlayer) {
                throw new IllegalStateException("Already spawning player????");
            }
            spawningPlayer = true;
        } finally {
            lock.unlock();
        }
    }

    @ApiStatus.Internal
    public void endSpawnPlayer() {
        lock.lock();
        try {
            if (!spawningPlayer) {
                throw new IllegalStateException("Not spawning player????");
            }
            spawningPlayer = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears all chunks in the queue and resends all tracked chunks.
     * Mainly used after a player respawn
     */
    public void resendAllVisibleChunks() {
        lock.lock();
        try {
            if (this.tracked == null) {
                throw new IllegalStateException("Tried to resend all chunks when tracking is disabled. This is most likely a logic error.");
            }
            if (spawningPlayer) {
                // Only send chunks if we are not spawning the player. (setInstance)
                // If we are spawning, the tracked object will change.
                return;
            }

            for (var it = chunksSentOrInQueue.longIterator(); it.hasNext(); ) {
                var chunkIndex = it.nextLong();
                var chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                var chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                player.getChunkQueue().cancelSend(chunkX, chunkZ);
            }
            chunksSentOrInQueue.clear();

            sendStale();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Starts tracking the current chunk (adds a claim)
     * <p>
     * Always called from player tick thread
     */
    public void startTracking(Tracked tracked) {
        lock.lock();
        try {
            if (this.tracked != null) {
                throw new IllegalStateException("Tried to start tracking when already tracking. This is most likely a logic error.");
            }
            this.tracked = tracked;
            sendStale();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops tracking the current chunk (removes the claim)
     * <p>
     * Always called from player tick thread
     */
    public void stopTracking() {
        lock.lock();
        try {
            if (tracked == null) {
                throw new IllegalStateException("Tried to stop tracking when nothing was being tracked. This is most likely a logic error.");
            }
            tracked.untrack();
            tracked = null;
        } finally {
            lock.unlock();
        }
        // We can do this outside the lock:
        // It is illegal for the callbacks to change visibleChunks if tracked is null
        // By doing this outside the lock, we allow the callbacks to acquire the lock and do the check,
        // then the callbacks will decide not to do anything.
        var it = chunksSentOrInQueue.longIterator();
        while (it.hasNext()) {
            var chunkIndex = it.nextLong();
            var chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
            var chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
            unloadChunk(chunkX, chunkZ);
        }
        chunksSentOrInQueue.clear();
    }

    private void unloadChunk(int x, int z) {
        player.sendPacket(new UnloadChunkPacket(x, z));
    }

    private void sendStale() {
        assert tracked != null;
        for (var chunk : tracked.visibleChunks().values()) {
            staleCallbackCount.decrementAndGet();
            sendChunk(chunk);
        }
    }

    private ClaimCallbacks callbacks(Long2ObjectMap<Chunk> trackedVisibleChunks) {
        return new ClaimCallbacks() {
            @Override
            public void chunkLoaded(ChunkClaim claim, Chunk chunk) {
                var index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
                lock.lock();
                try {
                    trackedVisibleChunks.put(index, chunk);
                    if (tracked == null || tracked.chunkAndClaim().claim() != claim) {
                        // Stale callback for old claim
                        staleCallbackCount.incrementAndGet();
                        return;
                    }
                    sendChunk(chunk);
                } finally {
                    lock.unlock();
                }
            }
        };
    }

    private void sendChunk(Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var index = CoordConversion.chunkIndex(x, z);
        var sendChunk = chunksSentOrInQueue.add(index);
        if (sendChunk) {
            player.sendChunk(chunk);
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public Tracked addClaim(Instance instance, Point pos) {
        return addClaim(instance, pos.chunkX(), pos.chunkZ());
    }

    public Tracked addClaim(Instance instance, int chunkX, int chunkZ) {
        var chunkManager = instance.getChunkManager();
        var trackedVisibleChunks = new Long2ObjectOpenHashMap<Chunk>();
        var chunkAndClaim = chunkManager.addClaim(chunkX, chunkZ, this.player.effectiveViewDistance(), this.priority, CLAIM_SHAPE, callbacks(trackedVisibleChunks));
        return new Tracked(chunkManager, chunkAndClaim, trackedVisibleChunks);
    }

    /**
     * @param visibleChunks the chunks that should be visible to the player. These chunks may still be in the chunk (send) queue.
     */
    public record Tracked(ChunkManager chunkManager, ChunkAndClaim chunkAndClaim, Long2ObjectMap<Chunk> visibleChunks) {
        private void untrack() {
            chunkManager.removeClaim(chunkAndClaim.claim());
        }
    }
}
