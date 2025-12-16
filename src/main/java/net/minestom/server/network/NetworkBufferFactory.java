package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.foreign.Arena;
import java.util.function.Supplier;

/**
 * Factory for creating a {@link NetworkBuffer} through {@link NetworkBufferFactory#staticFactory()}
 * or {@link NetworkBufferFactory#resizeableFactory()}.
 * <br>
 * Useful for creating buffers with specific configuration like arenas, auto resizing, and registries.
 * <br>
 * Factories are immutable and can be used across threads if the {@link Arena} supports it.
 * You also shouldn't rely on the identity of them due to being a value class candidate.
 * <br>
 * For example, using a confined arena for a manged lifetime.
 * <pre>{@code
 * try (Arena arena = Arena.ofConfined()) {
 *      var factory = NetworkBuffer.Factory.staticFactory().arena(arena);
 *      NetworkBuffer buffer = factory.allocate(1024);
 *      // Do things with the buffer
 * }}</pre>
 */
public interface NetworkBufferFactory {
    /**
     * Gets the static factory where {@link #arena(Supplier)} is set.
     *
     * @return the static factory.
     */
    @Contract(pure = true)
    static NetworkBufferFactory staticFactory() {
        return NetworkBufferProvider.networkBufferProvider().createStaticFactory();
    }

    /**
     * Gets the resizeable factory where {@link #autoResize(NetworkBuffer.AutoResize)} is set and built off {@link #staticFactory()}
     * using the {@link #autoResize(NetworkBuffer.AutoResize)} of {@link NetworkBuffer.AutoResize#DOUBLE}.
     *
     * @return the resizeable factory.
     */
    @Contract(pure = true)
    static NetworkBufferFactory resizeableFactory() {
        return NetworkBufferProvider.networkBufferProvider().createResizeableFactory();
    }

    /**
     * Sets the arena used for allocations.
     * <br>
     * Otherwise, if left unset, the default arena will be used.
     *
     * @param arena the arena
     * @return the new factory
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferFactory arena(Arena arena);

    /**
     * Sets the new arena strategy.
     * Called when we want to reallocate memory to a fresh arena, for example, during copy or initialization.
     * <br>
     * Note you should use {@link #arena(Arena)} if you use a singleton instance.
     * <br>
     * Otherwise, if left unset, the default arena will be used.
     *
     * @param arenaSupplier the supplier
     * @return the new factory
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferFactory arena(Supplier<Arena> arenaSupplier);

    /**
     * Sets the auto-resizing strategy.
     * <br>
     * Otherwise, if left unset, the buffer will never be resized and is considered a static buffer
     * unless it's a {@link #resizeableFactory()}.
     *
     * @param autoResize the {@link NetworkBuffer.AutoResize} strategy
     * @return the new factory
     */
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferFactory autoResize(NetworkBuffer.AutoResize autoResize);

    /**
     * Sets a registry for buffers to use.
     *
     * @param registries the registry
     * @return the new factory
     */
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferFactory registry(Registries registries);

    /**
     * Builds a new network buffer from this factory with {@code length} allocated.
     *
     * @param length the size of the buffer, or initial size if {@link NetworkBuffer.AutoResize} is set.
     * @return the new network buffer
     */
    @Contract("_ -> new")
    NetworkBuffer allocate(long length);
}
