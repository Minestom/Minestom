package net.minestom.server.event.world;

import net.minestom.server.event.WorldEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a chunk in a World is unloaded.
 */
public class WorldChunkUnloadEvent extends WorldEvent {

    private final int chunkX, chunkZ;

    public WorldChunkUnloadEvent(@NotNull World world, int chunkX, int chunkZ) {
        super(world);
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
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

}
