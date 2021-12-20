package net.minestom.server.thread;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
@ApiStatus.Experimental
public interface ThreadProvider {
    ThreadProvider PER_CHUNk = new ThreadProvider() {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public int findThread(@NotNull Chunk chunk) {
            return counter.getAndIncrement();
        }
    };
    ThreadProvider PER_INSTANCE = new ThreadProvider() {
        private final Cache<Instance, Integer> cache = Caffeine.newBuilder().weakKeys().build();
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public int findThread(@NotNull Chunk chunk) {
            return cache.get(chunk.getInstance(), i -> counter.getAndIncrement());
        }
    };
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
