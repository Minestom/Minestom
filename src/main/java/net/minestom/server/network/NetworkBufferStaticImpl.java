package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class NetworkBufferStaticImpl extends NetworkBufferImpl {
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
    protected @Nullable AutoResize autoResize() {
        return null;
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
}
