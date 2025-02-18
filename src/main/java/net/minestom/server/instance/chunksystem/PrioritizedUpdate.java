package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * {@link PrioritizedUpdate#updateType} should only be used for prioritizing, not to execute different update functionality
 */
record PrioritizedUpdate(@NotNull UpdateType updateType, double priority, int x, int z, @NotNull Origin origin) {
    /**
     * This comparator first compares by update type, then by priority.
     * Update type order is:
     * - {@link UpdateType#REMOVE_CLAIM_EXPLICIT}
     * - {@link UpdateType#ADD_CLAIM_EXPLICIT}
     * - {@link UpdateType#UNLOAD_PROPAGATE}
     * - {@link UpdateType#LOAD_PROPAGATE}
     * <p>
     * In a specific update type, the order is descending by priority.
     */
    static final Comparator<PrioritizedUpdate> COMPARATOR = Comparator
            .comparing(PrioritizedUpdate::updateType)
            .thenComparingDouble(PrioritizedUpdate::priority)
            .thenComparingInt(p -> p.origin().x()) // required to use this in a sorted set
            .thenComparingInt(p -> p.origin().z()) // required to use this in a sorted set
            .thenComparingInt(PrioritizedUpdate::z) // required to use this in a sorted set
            .thenComparingInt(PrioritizedUpdate::x) // required to use this in a sorted set
            .thenComparingInt(p -> p.origin().claim().hashCode()) // make sure no updates get lost
            .reversed();

    public PrioritizedUpdate(@NotNull UpdateType updateType, double priority, int x, int z, int claimX, int claimZ, ChunkClaim claim) {
        this(updateType, priority, x, z, new Origin(claimX, claimZ, claim));
    }

    record Origin(int x, int z, @NotNull ChunkClaim claim) {
        @NotNull ChunkClaim.Shape shape() {
            return claim().shape();
        }

        double priority() {
            return claim().priority();
        }
    }
}