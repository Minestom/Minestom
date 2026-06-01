package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;

/**
 * Used for testing and visualizing the function of the chunk system
 */
interface InternalCallbacks {
    default void onAddClaim(int x, int z, ChunkClaim chunkClaim) {
    }

    default void onRemoveClaim(int x, int z, ChunkClaim chunkClaim) {
    }

    /**
     * The loading of a chunk has started
     */
    default void onLoadStarted(int x, int z) {
    }

    /**
     * The loading of a chunk has completed
     */
    default void onLoadCompleted(int x, int z) {
    }

    default void onLoadCancelled(int x, int z) {
    }

    /**
     * The generation of a chunk has started
     */
    default void onGenerationStarted(int x, int z) {
    }

    /**
     * The generation of a chunk has completed
     */
    default void onGenerationCompleted(int x, int z) {
    }

    /**
     * The unloading of a chunk has started
     */
    default void onUnloadStarted(int x, int z) {
    }

    /**
     * The unloading of a chunk has completed
     */
    default void onUnloadCompleted(int x, int z) {
    }

    default void addUpdate(int x, int z, UpdateType updateType) {
    }

    default void removeUpdate(int x, int z, UpdateType updateType) {
    }

    default void onSaveStarted(Chunk chunk) {
    }

    default void onSaveComplete(Chunk chunk) {
    }
}
