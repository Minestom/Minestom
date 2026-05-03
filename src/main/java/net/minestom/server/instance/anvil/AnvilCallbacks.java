package net.minestom.server.instance.anvil;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.Nullable;

public interface AnvilCallbacks {
    static AnvilCallbacks noop() {
        return new AnvilCallbacks() {
        };
    }

    /**
     * Called to initialize the chunk with some skylight
     */
    default void loadSkyLight(Chunk chunk, int sectionY, byte[] lightData) {
    }

    /**
     * Called to initialize the chunk with some skylight
     */
    default void loadBlockLight(Chunk chunk, int sectionY, byte[] lightData) {
    }

    default byte @Nullable [] getSkyLight(Chunk chunk, int sectionY) {
        return null;
    }

    default byte @Nullable [] getBlockLight(Chunk chunk, int sectionY) {
        return null;
    }
}
