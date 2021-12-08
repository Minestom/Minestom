package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which is regularly saved into a snapshot.
 */
public interface Snapshotable {
    // Last saved snapshot
    @NotNull Snapshot snapshot();

    // Trigger a snapshot update, references must be set before using #snapshot()
    void updateSnapshot(Snapshot.@NotNull Updater updater);

    void triggerSnapshotChange(Snapshotable snapshotable);
}
