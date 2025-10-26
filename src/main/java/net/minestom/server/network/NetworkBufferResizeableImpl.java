package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Supplier;

public final class NetworkBufferResizeableImpl extends NetworkBufferImpl {
    private final AutoResize autoResize;
    private final Supplier<Arena> arenaSupplier;

    private Arena arena;
    private MemorySegment segment;

    NetworkBufferResizeableImpl(Arena arena, MemorySegment segment, long readIndex, long writeIndex, AutoResize autoResize, @Nullable Registries registries, Supplier<Arena> arenaSupplier) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.segment = Objects.requireNonNull(segment, "segment");
        Check.argCondition(segment.address() == MemorySegment.NULL.address(), "Segment address cannot be NULL");
        Check.argCondition(segment.isReadOnly(), "segment is read only, should be static");
        this.autoResize = Objects.requireNonNull(autoResize, "autoResize");
        this.arenaSupplier = Objects.requireNonNull(arenaSupplier, "arenaSupplier");
        super(readIndex, writeIndex, registries);
    }

    @Override
    protected MemorySegment segment() {
        return segment;
    }

    @Override
    protected Arena arena() {
        return arena;
    }

    @Override
    protected AutoResize autoResize() {
        return autoResize;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void resize(long length) {
        Check.argCondition(length < 0, "Length must be non-negative found {0}", length);
        assertDummy();
        assertReadOnly();
        final long capacity = capacity();
        if (length < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (length == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final Arena arena = arenaSupplier.get(); // We need to use a new arena to allow the old one to deallocate.
        final MemorySegment newSegment = NetworkBufferAllocator.allocate(arena, length);
        MemorySegment.copy(this.segment, 0, newSegment, 0, capacity);
        this.segment = newSegment;
        this.arena = arena;
    }

    @Override
    public void trim() {
        assertDummy();
        assertReadOnly();
        final long readableBytes = readableBytes();
        if (readableBytes == capacity()) return;
        final Arena arena = this.arenaSupplier.get();
        final MemorySegment oldSegment = this.segment;
        final MemorySegment segment = NetworkBufferAllocator.allocate(arena, readableBytes);
        MemorySegment.copy(oldSegment, readIndex(), segment, 0, readableBytes);
        this.segment = segment;
        this.arena = arena;
        this.writeIndex(readableBytes);
        this.readIndex(0);
    }

    @Override
    public void ensureCapacity(long targetSize) {
        final long capacity = capacity();
        final AutoResize strategy = this.autoResize();
        final long newCapacity = strategy.resize(capacity, targetSize);
        if (newCapacity <= capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);
        if (targetSize > newCapacity) {
            throw new IndexOutOfBoundsException("Buffer is full below the target size: " + newCapacity + " -> " + targetSize);
        }
        resize(newCapacity);
    }

    @Override
    protected boolean isDummy() {
        return false;
    }


}
