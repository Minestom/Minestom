package net.minestom.server.network.foreign;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Immutable Factory for {@link NetworkBuffer} instances.
 *
 * @param arenaSupplier the supplier of the {@link Arena} to use for allocations
 * @param autoResize the auto-resize strategy to use, or {@code null} for no auto-resize
 * @param registries the registries to use, or {@code null} for no registries
 */
record NetworkBufferFactoryImpl(Supplier<Arena> arenaSupplier, @Nullable NetworkBuffer.AutoResize autoResize,
                                @Nullable Registries registries) implements NetworkBufferFactory {

    public NetworkBufferFactoryImpl {
        Objects.requireNonNull(arenaSupplier, "arenaSupplier");
    }

    @Override
    public NetworkBufferFactoryImpl arena(Arena arena) {
        Objects.requireNonNull(arena, "arena");
        final Supplier<Arena> arenaSupplier = () -> arena; // stable value/lazy constant
        return new NetworkBufferFactoryImpl(arenaSupplier, autoResize, registries);
    }

    @Override
    public NetworkBufferFactoryImpl arena(Supplier<Arena> arenaSupplier) {
        Objects.requireNonNull(arenaSupplier, "arenaSupplier");
        return new NetworkBufferFactoryImpl(arenaSupplier, autoResize, registries);
    }

    @Override
    public NetworkBufferFactoryImpl autoResize(NetworkBuffer.AutoResize autoResize) {
        Objects.requireNonNull(autoResize, "autoResize");
        return new NetworkBufferFactoryImpl(arenaSupplier, autoResize, registries);
    }

    @Override
    public NetworkBufferFactoryImpl registry(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return new NetworkBufferFactoryImpl(arenaSupplier, autoResize, registries);
    }

    @Override
    public NetworkBuffer allocate(long length) {
        final Arena arena = Objects.requireNonNull(arenaSupplier.get(), "arena");
        final MemorySegment segment = NetworkBufferSegmentAllocator.allocate(arena, length);
        if (autoResize != null) {
            return new NetworkBufferResizeableSegmentImpl(arena, segment, 0, 0, autoResize, arenaSupplier, registries);
        } else {
            return new NetworkBufferStaticSegmentImpl(arena, segment, 0, 0, registries);
        }
    }

}
