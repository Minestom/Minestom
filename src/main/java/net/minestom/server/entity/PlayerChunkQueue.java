package net.minestom.server.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongComparator;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.play.ChunkBatchFinishedPacket;
import net.minestom.server.network.packet.server.play.ChunkBatchStartPacket;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

public class PlayerChunkQueue {
    private final Player player;
    private final ReentrantLock lock = new ReentrantLock();
    // This may seem counterintuitive, but this queue will get replaced quite frequently.
    // When we have to re-sort everything, because the player moved, this queue will be replaced
    // entirely. The other option is to remove all elements, change the comparator,
    // then add all elements again, which is arguably worse.
    private Long2ObjectSortedMap<Chunk> chunkQueue = new Long2ObjectRBTreeMap<>(this.compareChunkDistance(0, 0));
    private boolean needsChunkPositionSync = true;
    private float targetChunksPerTick = 9f; // Always send 9 chunks immediately
    private float pendingChunkCount = 0f; // Number of chunks to send on the current tick (ie 0.5 means we cannot send a chunk yet, 1.5 would send a single chunk with a 0.5 remainder)
    private int maxChunkBatchLead = 1; // Maximum number of batches to send before waiting for a reply
    private int chunkBatchLead = 0; // Number of batches sent without a reply
    // The tracked chunk X and Z coordinates. Required for correct sorting behavior
    private int posChunkX;
    private int posChunkZ;

    public PlayerChunkQueue(Player player) {
        this.player = player;
    }

    /**
     * Enqueues a chunk to send to the player.
     * The chunk will not be sent immediately, but over time in batches.
     * <p>
     * May be called from any thread
     *
     * @param chunk the chunk to send to the player
     */
    public void enqueueChunk(@NotNull Chunk chunk) {
        if (!chunk.isLoaded()) return;
        lock.lock();
        try {
            chunkQueue.put(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), chunk);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancels an enqueued chunk send request.
     * After this method returns, either the packet has already been sent, or it will not be sent.
     * <p>
     * May be called from any thread
     *
     * @return true if a chunk was removed from the queue (canceled), false if there was no such chunk in the queue
     */
    public boolean cancelSend(int chunkX, int chunkZ) {
        lock.lock();
        try {
            return chunkQueue.remove(CoordConversion.chunkIndex(chunkX, chunkZ)) != null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sends chunks from this queue, respecting rate limits like {@link ServerFlag#MAX_CHUNKS_PER_TICK}
     * <p>
     * Always called on player thread
     */
    void sendPendingRateLimited() {
        // If we have sent the max # of batches without a reply, do nothing
        if (chunkBatchLead >= maxChunkBatchLead) return;

        // Increment the pending chunk count by the target chunks per tick
        pendingChunkCount = Math.min(pendingChunkCount + targetChunksPerTick, ServerFlag.MAX_CHUNKS_PER_TICK);
        if (pendingChunkCount < 1) return; // Can't send anything

        lock.lock();

        // The position of the player may have changed, and if it has, we have to resort everything in the queue
        this.resortChangedPosition();

        try {
            // Queue is empty, do nothing
            if (chunkQueue.isEmpty()) return;

            player.sendPacket(new ChunkBatchStartPacket());
            int batchSize = 0;
            while (!chunkQueue.isEmpty() && pendingChunkCount >= 1F) {
                var chunk = chunkQueue.pollFirstEntry().getValue();
                if (!chunk.isLoaded()) continue;
                player.sendPacket(chunk.getFullDataPacket());

                EventDispatcher.call(new PlayerChunkLoadEvent(player, chunk.getChunkX(), chunk.getChunkZ()));

                pendingChunkCount--;
                batchSize++;
            }
            player.sendPacket(new ChunkBatchFinishedPacket(batchSize));
            chunkBatchLead++;

            if (needsChunkPositionSync) {
                player.synchronizePositionAfterTeleport(player.getPosition(), Vec.ZERO, RelativeFlags.NONE, true);
                needsChunkPositionSync = false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always called on player thread
     */
    void batchReceived(float newTargetChunksPerTick) {
        chunkBatchLead -= 1;
        targetChunksPerTick = Float.isNaN(newTargetChunksPerTick) ? ServerFlag.MIN_CHUNKS_PER_TICK : MathUtils.clamp(
                newTargetChunksPerTick * ServerFlag.CHUNKS_PER_TICK_MULTIPLIER, ServerFlag.MIN_CHUNKS_PER_TICK, ServerFlag.MAX_CHUNKS_PER_TICK);

        // Beyond the first batch, we can preemptively send up to 10 (matching mojang server)
        if (maxChunkBatchLead == 1) maxChunkBatchLead = 10;
    }

    private void resortChangedPosition() {
        var currentPos = player.getPosition();
        var chunkX = currentPos.chunkX();
        var chunkZ = currentPos.chunkZ();
        if (posChunkX == chunkX && posChunkZ == chunkZ) return; // Nothing changed, player didn't move

        posChunkX = chunkX;
        posChunkZ = chunkZ;
        // Re-add everything to a new queue with a different comparator
        // This should free the old queue to be garbage collected
        var newQueue = new Long2ObjectRBTreeMap<Chunk>(compareChunkDistance(chunkX, chunkZ));
        newQueue.putAll(chunkQueue);
        chunkQueue = newQueue;
    }

    private LongComparator compareChunkDistance(int x, int z) {
        return (long chunkIndexA, long chunkIndexB) -> {
            int chunkAX = CoordConversion.chunkIndexGetX(chunkIndexA);
            int chunkAZ = CoordConversion.chunkIndexGetZ(chunkIndexA);
            int chunkBX = CoordConversion.chunkIndexGetX(chunkIndexB);
            int chunkBZ = CoordConversion.chunkIndexGetZ(chunkIndexB);
            int chunkDistanceA = Math.abs(chunkAX - x) + Math.abs(chunkAZ - z);
            int chunkDistanceB = Math.abs(chunkBX - x) + Math.abs(chunkBZ - z);
            return Integer.compare(chunkDistanceA, chunkDistanceB);
        };
    }
}
