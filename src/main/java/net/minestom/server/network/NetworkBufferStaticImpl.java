package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Represents a {@link NetworkBuffer#staticBuffer(long, Registries)}.
 * <br>
 * Not resizeable, including shrinking, but have the benefit of final {@link Arena} and {@link MemorySegment}.
 * Which makes this an excellent wrapper class for {@link #wrap(MemorySegment, int, int, Registries)}.
 */
@ApiStatus.Internal
final class NetworkBufferStaticImpl extends NetworkBufferImpl {
    private final @Nullable Arena arena;
    private final MemorySegment segment;

    NetworkBufferStaticImpl(@Nullable Arena arena, MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
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
    protected boolean isDummy() {
        return segment == MemorySegment.NULL;
    }

    @Override
    public void ensureCapacity(long requestedSize) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void resize(long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trim() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NetworkBufferStaticImpl that)) return false;
        return Objects.equals(arena, that.arena) && segment.equals(that.segment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(arena);
        result = 31 * result + segment.hashCode();
        return result;
    }
}
