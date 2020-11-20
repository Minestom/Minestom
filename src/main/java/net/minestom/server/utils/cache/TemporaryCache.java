package net.minestom.server.utils.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cache objects with a timeout.
 *
 * @param <T> the object type to cache
 */
public class TemporaryCache<T> {

    public static final ScheduledExecutorService REMOVER_SERVICE = Executors.newScheduledThreadPool(1);

    // Identifier = Cached object
    protected ConcurrentHashMap<UUID, T> cache = new ConcurrentHashMap<>();
    // Identifier = time
    protected ConcurrentHashMap<UUID, Long> cacheTime = new ConcurrentHashMap<>();

    private long keepTime;

    /**
     * Creates a new temporary cache.
     *
     * @param keepTime the time before considering an object unused in milliseconds
     * @see #getKeepTime()
     */
    public TemporaryCache(long keepTime) {
        this.keepTime = keepTime;
        REMOVER_SERVICE.scheduleAtFixedRate(() -> {
            final boolean removed = cacheTime.values().removeIf(time -> time + keepTime > System.currentTimeMillis());
            if (removed) {
                this.cache.entrySet().removeIf(entry -> !cacheTime.containsKey(entry.getKey()));
            }
        }, keepTime, keepTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Caches an object
     *
     * @param identifier the object identifier
     * @param value      the object to cache
     * @param time       the current time in milliseconds
     */
    public synchronized void cacheObject(@NotNull UUID identifier, T value, long time) {
        this.cache.put(identifier, value);
        this.cacheTime.put(identifier, time);
    }

    /**
     * Retrieves an object from cache.
     *
     * @param identifier the object identifier
     * @return the retrieved object or null if not found
     */
    @Nullable
    public T retrieve(@NotNull UUID identifier) {
        return cache.get(identifier);
    }

    /**
     * Gets the time an object will be kept without being retrieved
     *
     * @return the keep time in milliseconds
     */
    public long getKeepTime() {
        return keepTime;
    }
}
