package net.minestom.server.network.foreign;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Represents a {@link NetworkBuffer#staticBuffer(long, Registries)}.
 * <br>
 * Not resizeable, including shrinking, but has the benefit of final {@link Arena} and {@link MemorySegment}.
 * Which makes this an excellent wrapper class for {@link NetworkBuffer#wrap(MemorySegment, long, long, Registries)}.
 */
@ApiStatus.Internal
final class NetworkBufferStaticSegmentImpl extends NetworkBufferSegmentImpl {
    private final @Nullable Arena arena;
    private final MemorySegment segment;

    NetworkBufferStaticSegmentImpl(@Nullable Arena arena, MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        this.arena = arena;
        this.segment = Objects.requireNonNull(segment, "segment");
        super(readIndex, writeIndex, registries);
    }

    @Override
    protected MemorySegment segment() {
        return segment;
    }

    @Override
    protected @Nullable Arena arena() {
        return arena;
    }

    @Override
    public boolean isReadOnly() {
        return segment().isReadOnly();
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public void resize(long length) {
        throw new UnsupportedOperationException("Static buffer cannot be resized to %d".formatted(length));
    }

    @Override
    public boolean requestCapacity(long targetSize) {
        return false;
    }

    @Override
    public void trim() {
        throw new UnsupportedOperationException("Static buffer cannot resized for trim");
    }
}
