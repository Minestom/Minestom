package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a chunk in an instance is unloaded.
 */
public class InstanceChunkUnloadEvent implements InstanceEvent {

    private final Instance instance;
    private final Chunk chunk;

    private final int chunkX;
    private final int chunkZ;

    public InstanceChunkUnloadEvent(@NotNull Instance instance, @NotNull Chunk chunk, int chunkX, int chunkZ) {
        this.instance = instance;
        this.chunk = chunk;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Gets the chunk.
     *
     * @return the chunk.
     */
    public @NotNull Chunk getChunk() {
        return chunk;
    }
}
