package net.minestom.server.network;

import net.minestom.server.network.foreign.NetworkBufferSegmentProvider;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.foreign.Arena;

/**
 * Factory like object for creating a {@link NetworkBuffer} through {@link NetworkBufferAllocator#staticAllocator()}
 * or {@link NetworkBufferAllocator#resizeableAllocator()}.
 * <br>
 * Useful for creating buffers with specific configuration like arenas, auto resizing, and registries.
 * <br>
 * Factories are immutable and can be used across threads if the {@link Arena} supports it.
 * You also shouldn't rely on the identity of them due to being a value class candidate.
 * <br>
 * For example, using a confined arena for a manged lifetime.
 * <pre>{@code
 * try (Arena arena = Arena.ofConfined()) {
 *      var allocator = NetworkBufferAllocator.staticAllocator().arena(arena);
 *      NetworkBuffer buffer = allocator.allocate(1024);
 *      // Do things with the buffer
 * }}</pre>
 */
public interface NetworkBufferAllocator {
    /**
     * Gets the static allocator where {@link #arena(ArenaStrategy)} is set.
     *
     * @return the static allocator.
     */
    @Contract(pure = true)
    static NetworkBufferAllocator staticAllocator() {
        return NetworkBufferSegmentProvider.INSTANCE.staticAllocator();
    }

    /**
     * Gets the resizeable allocator where {@link #autoResize(NetworkBuffer.AutoResize)} is set and built off {@link #staticAllocator()}
     * using the {@link #autoResize(NetworkBuffer.AutoResize)} of {@link NetworkBuffer.AutoResize#DOUBLE}.
     *
     * @return the resizeable allocator.
     */
    @Contract(pure = true)
    static NetworkBufferAllocator resizeableAllocator() {
        return NetworkBufferSegmentProvider.INSTANCE.resizeableAllocator();
    }

    /**
     * Sets the arena used for allocations.
     * <br>
     * Otherwise, if left unset, the default arena will be used.
     * <br>
     * The callee is responsible for releasing the arena.
     *
     * @param arena the arena
     * @return the new allocator
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferAllocator arena(Arena arena);

    /**
     * Sets the arena strategy.
     * Called when we want to reallocate memory to a fresh arena, for example, during copy or initialization.
     * <br>
     * Note you should use {@link #arena(Arena)} if you use a singleton instance.
     * <br>
     * Otherwise, if left unset, the default arena will be used.
     *
     * @param arenaStrategy the supplier
     * @return the new allocator
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferAllocator arena(ArenaStrategy arenaStrategy);

    /**
     * Sets the auto-resizing strategy.
     * <br>
     * Otherwise, if left unset, the buffer will never be resized and is considered a static buffer
     * unless it's a {@link #resizeableAllocator()}.
     *
     * @param autoResize the {@link NetworkBuffer.AutoResize} strategy
     * @return the new allocator
     */
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferAllocator autoResize(NetworkBuffer.AutoResize autoResize);

    /**
     * Sets a registry for buffers to use.
     *
     * @param registries the registry
     * @return the new allocator
     */
    @Contract(pure = true, value = "_ -> new")
    NetworkBufferAllocator registry(Registries registries);

    /**
     * Builds a new network buffer from this allocator with {@code length} allocated.
     *
     * @param length the size of the buffer, or initial size if {@link NetworkBuffer.AutoResize} is set.
     * @return the new network buffer
     */
    @Contract("_ -> new")
    NetworkBuffer allocate(long length);

    /**
     * A strategy for reallocating arenas.
     * <br>
     * Note: After releasing an arena, it can be recycled, but it's not gaurenteed to have no references to it.
     */
    @ApiStatus.Experimental
    interface ArenaStrategy {
        /**
         * Acquires an arena, which can be released later.
         *
         * @return the possibly new arena from the strategy
         */
        Arena acquire();

        /**
         * Releases an arena from the current context.
         * <br>
         * No guarantee to be called during garbage collection. Consider tracking all acquired arenas.
         *
         * @param arena the arena to release, gaurenteed the same as the one returned by {@link #acquire()}
         */
        default void release(Arena arena) {
            // Default implementation does nothing, for example, global or auto arenas.
        }
    }
}
