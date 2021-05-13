package net.minestom.server.utils.cache;

import com.google.common.annotations.Beta;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Represents a single that can be collected by the garbage collector
 * depending on memory demand.
 *
 * @param <T> the object to cache
 */
@Beta
public class CachedObject<T> extends SoftReference<AtomicReference<T>> {

    private long cacheTime;

    public CachedObject() {
        super(new AtomicReference<>());
    }

    /**
     * Retrieves the cache.
     *
     * @param supplier supplier for the value if absent
     * @return the cache
     */
    public @NotNull T getCache(@NotNull Supplier<@NotNull T> supplier) {
        var referent = get();
        assert referent != null;
        var value = referent.get();
        if (value == null) {
            value = supplier.get();
            referent.set(value);
        }
        return value;
    }

    /**
     * Retrieves the cache, and ensure that the value is up-to-date.
     *
     * @param supplier   supplier for the value if absent or outdated
     * @param lastUpdate the required internal timestamp, supplier will be called otherwise
     * @return the cache
     */
    public @NotNull T getUpdatedCache(@NotNull Supplier<@NotNull T> supplier, long lastUpdate) {
        var referent = get();
        assert referent != null;
        var value = referent.get();
        if (value == null || cacheTime != lastUpdate) {
            value = supplier.get();
            this.cacheTime = lastUpdate;
            referent.set(value);
        }
        return value;
    }

    /**
     * @deprecated use {@link #getCache(Supplier)}
     */
    @Override
    @Deprecated
    public AtomicReference<T> get() {
        return super.get();
    }
}
