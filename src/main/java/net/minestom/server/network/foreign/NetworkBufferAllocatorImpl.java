package net.minestom.server.network.foreign;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferAllocator;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Immutable allocator for {@link NetworkBuffer} instances.
 *
 * @param arenaStrategy the supplier of the {@link Arena} to use for allocations
 * @param autoResize the auto-resize strategy to use, or {@code null} for no auto-resize
 * @param registries the registries to use, or {@code null} for no registries
 */
@ApiStatus.Internal
record NetworkBufferAllocatorImpl(ArenaStrategy arenaStrategy, @Nullable NetworkBuffer.AutoResize autoResize,
                                  @Nullable Registries registries) implements NetworkBufferAllocator {

    public NetworkBufferAllocatorImpl {
        Objects.requireNonNull(arenaStrategy, "arenaStrategy");
    }

    @Override
    public NetworkBufferAllocatorImpl arena(Arena arena) {
        Objects.requireNonNull(arena, "arena");
        final ArenaStrategy arenaStrategy = new FixedArenaStrategy(arena);
        return new NetworkBufferAllocatorImpl(arenaStrategy, autoResize, registries);
    }

    @Override
    public NetworkBufferAllocator arena(ArenaStrategy arenaStrategy) {
        Objects.requireNonNull(arenaStrategy, "arenaStrategy");
        return new NetworkBufferAllocatorImpl(arenaStrategy, autoResize, registries);
    }

    @Override
    public NetworkBufferAllocatorImpl autoResize(NetworkBuffer.AutoResize autoResize) {
        Objects.requireNonNull(autoResize, "autoResize");
        return new NetworkBufferAllocatorImpl(arenaStrategy, autoResize, registries);
    }

    @Override
    public NetworkBufferAllocatorImpl registry(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return new NetworkBufferAllocatorImpl(arenaStrategy, autoResize, registries);
    }

    @Override
    public NetworkBufferSegmentImpl allocate(long length) {
        final Arena arena = Objects.requireNonNull(arenaStrategy.acquire(), "arena");
        final MemorySegment segment = NetworkBufferNativeSegmentAllocator.allocate(arena, length);
        if (autoResize != null) {
            return new NetworkBufferResizeableSegmentImpl(arena, segment, 0, 0, autoResize, arenaStrategy, registries);
        } else {
            return new NetworkBufferStaticSegmentImpl(arena, segment, 0, 0, registries);
        }
    }

    record FixedArenaStrategy(Arena arena) implements ArenaStrategy {
        public FixedArenaStrategy {
            Objects.requireNonNull(arena, "arena");
        }

        @Override
        public Arena acquire() {
            return arena;
        }
    }
}
