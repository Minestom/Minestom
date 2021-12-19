package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public sealed interface DispatchUpdate permits
        DispatchUpdate.ChunkLoad, DispatchUpdate.ChunkUnload,
        DispatchUpdate.EntityUpdate, DispatchUpdate.EntityRemove {
    record ChunkLoad(@NotNull Chunk chunk) implements DispatchUpdate {
    }

    record ChunkUnload(@NotNull Chunk chunk) implements DispatchUpdate {
    }

    record EntityUpdate(@NotNull Entity entity) implements DispatchUpdate {
    }

    record EntityRemove(@NotNull Entity entity) implements DispatchUpdate {
    }
}
