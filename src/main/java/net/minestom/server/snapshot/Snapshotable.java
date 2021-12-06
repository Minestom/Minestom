package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which is regularly saved into a snapshot.
 */
public interface Snapshotable {
    /**
     * Gets the last saved snapshot.
     *
     * @return the last saved snapshot
     */
    @NotNull Snapshot snapshot();
}
