package net.minestom.server.instance.chunksystem;

import java.util.Comparator;

/**
 * {@link PrioritizedUpdate#updateType} should only be used for prioritizing, not to execute different update functionality
 */
record PrioritizedUpdate(UpdateType updateType, double priority, int x, int z) {
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
            .reversed();
}