

package net.minestom.server.snapshot;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;

/**
 * Represents the complete state of the server at a given moment.
 */
public sealed interface ServerSnapshot extends Snapshot
        permits SnapshotImpl.Server {
    @NotNull Collection<@NotNull InstanceSnapshot> instances();

    @NotNull Collection<EntitySnapshot> entities();

    @UnknownNullability EntitySnapshot entity(int id);

    @ApiStatus.Experimental
    static ServerSnapshot update() {
        return SnapshotUpdater.update(MinecraftServer.process());
    }
}
