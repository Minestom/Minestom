package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UpdateQueue {
    /**
     * A sorted set instead of a priority queue: Duplicate updates can add up quickly and cause OOMEs.
     * Instead of tracking duplicates manually (with a second Set), we can also just use a sorted set,
     * which is more appropriate
     */
    private final ObjectSortedSet<PrioritizedUpdate> updateQueue = new ObjectRBTreeSet<>(PrioritizedUpdate.COMPARATOR);
    private final ObjectSet<PrioritizedUpdate> disabledPropagation = new ObjectOpenHashSet<>();
    private final SingleThreadedManager singleThreadedManager;
    /**
     * The size of the updateQueue after the last update
     */
    private int lastUpdateQueueCleanupSize = 0;
    private boolean lastRemovedDisablePropagation = false;
    private boolean updated = false;

    public UpdateQueue(SingleThreadedManager singleThreadedManager) {
        this.singleThreadedManager = singleThreadedManager;
    }

    void enqueue(PrioritizedUpdate update, @Nullable SingleThreadedManager.ClaimData claimData) {
        enqueue(update, claimData, false);
    }

    void enqueue(PrioritizedUpdate update, @Nullable SingleThreadedManager.ClaimData claimData, boolean disablePropagation) {
        // A similar update may already be in the queue
        if (!this.updateQueue.add(update)) return;
        this.updated = true;
        if (claimData != null) {
            claimData.updateEnqueued(update);
        }
        if (disablePropagation) {
            this.disabledPropagation.add(update);
        }

        if (SingleThreadedManager.callbacks != null) {
            SingleThreadedManager.callbacks.addUpdate(update.x(), update.z(), update.updateType());
        }

        // Periodic cleanup based on size to release out-of-date data/memory when the system is under pressure
        if (this.updateQueue.size() > (this.lastUpdateQueueCleanupSize << 2) + 100) {
            // We use this formula to make sure we don't do updates too often.
            cleanupUpdateQueue();
            this.lastUpdateQueueCleanupSize = this.updateQueue.size();
        }
    }

    @Nullable PrioritizedUpdate dequeue() {
        if (this.updateQueue.isEmpty()) return null;
        var update = this.updateQueue.removeFirst();
        this.lastRemovedDisablePropagation = this.disabledPropagation.remove(update);
        if (SingleThreadedManager.callbacks != null) {
            SingleThreadedManager.callbacks.removeUpdate(update.x(), update.z(), update.updateType());
        }
        return update;
    }

    public boolean lastRemovedDisablePropagation() {
        return this.lastRemovedDisablePropagation;
    }

    int size() {
        return this.updateQueue.size();
    }

    /**
     * Resets updated to false
     *
     * @return whether an element has been enqueued since last call to resetUpdate
     */
    boolean resetUpdated() {
        var updated = this.updated;
        this.updated = false;
        return updated;
    }

    boolean updated() {
        return updated;
    }

    private boolean propagateUpdate(@NotNull ChunkClaim origin, @Nullable SingleThreadedManager.ClaimData claimData, double originUpdatePriority, int x, int z, UpdateType updateType) {
        if (!propagatesTo(origin, originUpdatePriority, x, z, updateType)) return false;
        var tree = this.singleThreadedManager.tree;
        var priorityDrop = this.singleThreadedManager.priorityDrop;

        var priority = tree.calculatePriority(priorityDrop, origin.priority(), origin.chunkX(), origin.chunkZ(), x, z);
        this.enqueue(new PrioritizedUpdate(updateType, priority, x, z, origin), claimData);
        return true;
    }

    private boolean propagatesTo(@NotNull ChunkClaim origin, double fromUpdatePriority, int x, int z, UpdateType updateType) {
        if (!origin.contains(x, z)) {
            return false;
        }

        if (updateType != UpdateType.UNLOAD_PROPAGATE) {
            if (!this.singleThreadedManager.hasClaim(origin)) {
                // claim no longer exists
                return false;
            }
        }

        var priorityDrop = this.singleThreadedManager.priorityDrop;

        var tree = this.singleThreadedManager.tree;
        // We need an entry with the given priority at the chunk
        var requiredPriority = tree.calculatePriority(priorityDrop, origin.priority(), origin.chunkX(), origin.chunkZ(), x, z);
        return requiredPriority + Vec.EPSILON < fromUpdatePriority;
    }

    void propagateUpdates(@NotNull PrioritizedUpdate originUpdate, @Nullable SingleThreadedManager.ClaimData claimData, boolean disablePropagation) {
        if (disablePropagation) return;
        propagateUpdates(originUpdate, claimData);
    }

    private void propagateUpdates(@NotNull PrioritizedUpdate originUpdate, @Nullable SingleThreadedManager.ClaimData claimData) {
        var x = originUpdate.x();
        var z = originUpdate.z();
        var origin = originUpdate.origin();
        var updateType = originUpdate.updateType().propagated();
        var originUpdatePriority = originUpdate.priority();
        this.propagateUpdate(origin, claimData, originUpdatePriority, x + 1, z + 1, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x + 1, z, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x + 1, z - 1, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x, z - 1, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x - 1, z - 1, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x - 1, z, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x - 1, z + 1, updateType);
        this.propagateUpdate(origin, claimData, originUpdatePriority, x, z + 1, updateType);
    }

    /**
     * Removes all stale updates from the queue (TODO)
     * Should help with memory consumption, in case that becomes an issue
     */
    private void cleanupUpdateQueue() {
    }
}
