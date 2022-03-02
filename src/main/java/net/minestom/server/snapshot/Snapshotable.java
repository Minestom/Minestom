package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which is regularly saved into a snapshot.
 */
public interface Snapshotable {
    /**
     * Retrieves the last snapshot.
     * <p>
     * This method does not need to be thread safe assuming
     * {@link #updateSnapshot(SnapshotUpdater)} is only called at safe-points.
     *
     * @return the last snapshot
     */
    @NotNull Snapshot snapshot();

    /**
     * Updates the currently cached snapshot if required.
     * The updater can be used to retrieve references to other snapshots while avoiding circular dependency.
     * Be careful to do not store {@code updater} anywhere as its data will change when building requested references.
     * <p>
     * The return value of {@link #snapshot()} must be updated to reflect changes.
     * <p>
     * This method is not thread-safe, and targeted at internal use
     * since its execution rely on safe-points (e.g. end of ticks)
     *
     * @param updater the snapshot updater/context
     */
    void updateSnapshot(@NotNull SnapshotUpdater updater);
}
