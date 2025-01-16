package net.minestom.server.snapshot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which is regularly saved into a snapshot.
 * <p>
 * Implementations must be identity-based.
 */
@ApiStatus.Experimental
public interface Snapshotable {

    /**
     * Updates the currently cached snapshot if required.
     * The updater can be used to retrieve references to other snapshots while avoiding circular dependency.
     * Be careful to do not store {@code updater} anywhere as its data will change when building requested references.
     * <p>
     * This method is not thread-safe, and targeted at internal use
     * since its execution rely on safe-points (e.g. end of ticks)
     *
     * @param updater the snapshot updater/context
     * @return the updated snapshot
     */
    default @NotNull Snapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        throw new UnsupportedOperationException("Snapshot is not supported for this object");
    }
}
