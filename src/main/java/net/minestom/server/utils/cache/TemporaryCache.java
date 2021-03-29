package net.minestom.server.utils.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Cache objects with a timeout.
 *
 * @param <T> the object type to cache
 */
public class TemporaryCache<T> {

    private final Cache<UUID, T> cache;

    /**
     * Creates a new temporary cache.
     *
     * @param duration the time before considering an object unused
     */
    public TemporaryCache(long duration, TimeUnit timeUnit, RemovalListener<UUID, T> removalListener) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(duration, timeUnit)
                .softValues()
                .removalListener(removalListener)
                .build();
    }

    /**
     * Caches an object.
     *
     * @param identifier the object identifier
     * @param value      the object to cache
     */
    public void cache(@NotNull UUID identifier, T value) {
        this.cache.put(identifier, value);
    }

    /**
     * Retrieves an object from cache.
     *
     * @param identifier the object identifier
     * @return the retrieved object or null if not found
     */
    @Nullable
    public T retrieve(@NotNull UUID identifier) {
        return cache.getIfPresent(identifier);
    }
}
