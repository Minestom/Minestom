package net.minestom.server.utils.cache;

import net.minestom.server.thread.MinestomThread;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Faster alternative to {@link ThreadLocal} when called from a {@link MinestomThread}.
 * Idea took from Netty's FastThreadLocal.
 * <p>
 * Must not be abused, as the underlying array is not downsized.
 * Mostly for internal use.
 *
 * @param <T> the type to cache
 */
@ApiStatus.Internal
public final class LocalCache<T> {
    private final int tickIndex = MinestomThread.LOCAL_COUNT.getAndIncrement();
    private final Supplier<T> supplier;
    private final ThreadLocal<T> fallback;

    private LocalCache(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
        this.fallback = ThreadLocal.withInitial(supplier);
    }

    public static LocalCache<ByteBuffer> ofBuffer(int size) {
        return new LocalCache<>(() -> ByteBuffer.allocateDirect(size));
    }

    public T get() {
        Thread current = Thread.currentThread();
        if (current instanceof MinestomThread) {
            return ((MinestomThread) current).localCache(tickIndex, supplier);
        }
        return fallback.get();
    }
}
