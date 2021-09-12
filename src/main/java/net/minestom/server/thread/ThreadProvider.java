package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@ApiStatus.Experimental
public interface ThreadProvider {
    ThreadProvider PER_CHUNk = Object::hashCode;
    ThreadProvider PER_INSTANCE = chunk -> chunk.getInstance().hashCode();
    ThreadProvider SINGLE = chunk -> 0;

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param chunk the chunk
     */
    int findThread(@NotNull Chunk chunk);

    /**
     * Defines how often chunks thread should be updated.
     *
     * @return the refresh type
     */
    default @NotNull RefreshType getChunkRefreshType() {
        return RefreshType.NEVER;
    }

    /**
     * Defines how often chunks thread should be refreshed.
     */
    enum RefreshType {
        /**
         * Chunk thread is constant after being defined.
         */
        NEVER,
        /**
         * Chunk thread should be recomputed as often as possible.
         */
        CONSTANT,
        /**
         * Chunk thread should be recomputed, but not continuously.
         */
        RARELY
    }
}
