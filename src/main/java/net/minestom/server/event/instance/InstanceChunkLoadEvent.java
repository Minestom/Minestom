package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a chunk in an instance is loaded.
 */
public record InstanceChunkLoadEvent(@NotNull Instance instance, @NotNull Chunk chunk) implements InstanceEvent {

    @Override
    public @NotNull Instance instance() {
        return instance;
    }

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunk.getChunkX();
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunk.getChunkZ();
    }

    /**
     * Gets the chunk.
     *
     * @return the chunk.
     */
    @Override
    public @NotNull Chunk chunk() {
        return chunk;
    }
}
