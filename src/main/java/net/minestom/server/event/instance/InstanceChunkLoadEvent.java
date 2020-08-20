package net.minestom.server.event.instance;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Called when a chunk in an instance is loaded
 */
public class InstanceChunkLoadEvent extends Event {

    private final Instance instance;
    private final int chunkX, chunkZ;

    public InstanceChunkLoadEvent(Instance instance, int chunkX, int chunkZ) {
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Get the instance where the chunk has been loaded
     *
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * Get the chunk X
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Get the chunk Z
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }
}
