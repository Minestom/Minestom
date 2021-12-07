package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which is regularly saved into a snapshot.
 */
public interface Snapshotable {
    // Last saved snapshot
    @NotNull Snapshot snapshot();

    // Update and return the snapshot from #snapshot()
    @NotNull Snapshot updatedSnapshot();
}
