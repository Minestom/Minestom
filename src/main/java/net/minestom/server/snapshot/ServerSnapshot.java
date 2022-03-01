

package net.minestom.server.snapshot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents the complete state of the server at a given moment.
 */
public interface ServerSnapshot extends Snapshot {
    @NotNull Collection<@NotNull InstanceSnapshot> instances();

    static @NotNull ServerSnapshot get() {
        return ServerSnapshotImpl.get();
    }

    @ApiStatus.Internal
    static void update() {
        ServerSnapshotImpl.update();
    }
}
