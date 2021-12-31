package net.minestom.server.thread;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
@ApiStatus.Experimental
public interface ThreadProvider<T> {
    static <T> @NotNull ThreadProvider<T> counter() {
        return new ThreadProvider<>() {
            private final Cache<T, Integer> cache = Caffeine.newBuilder().weakKeys().build();
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public int findThread(@NotNull T partition) {
                return cache.get(partition, i -> counter.getAndIncrement());
            }
        };
    }

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param partition the partition
     */
    int findThread(@NotNull T partition);

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
