package net.minestom.server.entity;

import it.unimi.dsi.fastutil.longs.*;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import net.minestom.server.instance.chunksystem.ClaimCallbacks;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsible for loading chunks around and sending chunks to the player
 */
public class PlayerChunkTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerChunkTracker.class);
    private final Player player;
    // Even though adding/removing claims is done on the player thread, we need this lock for the callbacks and updating visibleChunks
    private final ReentrantLock lock = new ReentrantLock();
    private final LongSet visibleChunks = new LongOpenHashSet();
    private @Nullable Tracked tracked;
    private volatile int priority = 0;

    public PlayerChunkTracker(Player player) {
        this.player = player;
    }

    public void changePosition() {

    }

    /**
     * Changes the tracked chunk. (Basically moves a claim)
     * <p>
     * Always called from player tick thread
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
            this.tracked.chunkManager.removeClaim(this.tracked.claim);

            // The simplest approach is to iterate through all chunks. Let's use that and see how well it does
            var it = visibleChunks.longIterator();
            while (it.hasNext()) {
                var chunkIndex = it.nextLong();
                var chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                var chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                if (tracked.claim.contains(chunkX, chunkZ)) {
                    // Chunk is visible in the new claim
                    continue;
                }
                player.getChunkQueue().cancelSend(chunkX, chunkZ);
                if (sendUnloads) {
                    unloadChunk(chunkX, chunkZ);
                } else { // Only call events
                    eventUnload(chunkX, chunkZ);
                }
            }

            this.tracked = tracked;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Starts tracking the current chunk (adds a claim)
     * <p>
     * Always called from player tick thread
     */
    public void startTracking() {
        lock.lock();
        try {
            if (tracked != null) {
                throw new IllegalStateException("Tried to start tracking when already tracking. This is most likely a logic error.");
            }
            tracked = addClaim(player.getInstance(), player.getPosition());
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
            tracked.chunkManager.removeClaim(tracked.claim);
            tracked = null;
        } finally {
            lock.unlock();
        }
        // We can do this outside the lock:
        // It is illegal for the callbacks to change visibleChunks if tracked is null
        // By doing this outside the lock, we allow the callbacks to acquire the lock and do the check,
        // then the callbacks will decide not to do anything.
        var it = visibleChunks.longIterator();
        while (it.hasNext()) {
            var chunkIndex = it.nextLong();
            var chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
            var chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
            unloadChunk(chunkX, chunkZ);
        }
        visibleChunks.clear();
    }

    private void unloadChunk(int x, int z) {
        player.sendPacket(new UnloadChunkPacket(x, z));
        eventUnload(x, z);
    }

    /**
     * This is a separate method from {@link #unloadChunk(int, int)}, because when respawning we don't send the packets.
     * We should still, however, call the events, considering chunks are being unloaded for the player.
     */
    private void eventUnload(int x, int z) {
        EventDispatcher.call(new PlayerChunkUnloadEvent(player, x, z));
    }

    private ClaimCallbacks callbacks(Long2ObjectMap<Chunk> trackedVisibleChunks) {
        return new ClaimCallbacks() {
            @Override
            public void chunkLoaded(@NotNull ChunkClaim claim, @NotNull Chunk chunk) {
                var index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
                boolean sendChunk;
                lock.lock();
                try {
                    trackedVisibleChunks.put(index, chunk);
                    if (tracked == null || tracked.claim != claim) {
                        // Stale callback for old claim
                        return;
                    }
                    sendChunk = visibleChunks.add(index);
                } finally {
                    lock.unlock();
                }
                if (sendChunk) {
                    player.sendChunk(chunk);
                }
            }
        };
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
        var chunkAndClaim = chunkManager.addClaim(chunkX, chunkZ, this.player.getSettings().effectiveViewDistance(), this.priority, ChunkClaim.Shape.CIRCLE, callbacks(trackedVisibleChunks));
        var claim = chunkAndClaim.claim();
        return new Tracked(chunkManager, claim, trackedVisibleChunks);
    }

    public record Tracked(@NotNull ChunkManager chunkManager, @NotNull ChunkClaim claim,
                          @NotNull Long2ObjectMap<Chunk> visibleChunks) {
    }
}
