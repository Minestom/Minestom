package net.minestom.server.network.foreign;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents {@link #resizableBuffer(long, Registries)}.
 * <br>
 * Which are the most commonly used when the bounds of the application are unknown for example {@link #makeArray(Type, Object)}.
 * These will be converted into {@link NetworkBufferStaticSegmentImpl} if applicable, for example {@link #readOnly()}.
 */
@ApiStatus.Internal
final class NetworkBufferResizeableSegmentImpl extends NetworkBufferSegmentImpl {
    private final AutoResize autoResize;
    private final Supplier<? extends Arena> arenaSupplier;

    private Arena arena;
    private MemorySegment segment;

    NetworkBufferResizeableSegmentImpl(Arena arena, MemorySegment segment, long readIndex, long writeIndex, AutoResize autoResize, Supplier<? extends Arena> arenaSupplier, @Nullable Registries registries) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.segment = Objects.requireNonNull(segment, "segment");
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
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(long length) {
        Check.argCondition(length < 0, "Length must be non-negative found {0}", length);
        final long capacity = capacity();
        if (length < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (length == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final Arena arena = arenaSupplier.get(); // We need to use a new arena to allow the old one to deallocate.
        final MemorySegment newSegment = NetworkBufferSegmentAllocator.allocate(arena, length);
        MemorySegment.copy(this.segment, 0, newSegment, 0, capacity);
        this.segment = newSegment;
        this.arena = arena;
    }

    @Override
    public boolean requestCapacity(long targetSize) {
        final long capacity = capacity();
        final long newCapacity = this.autoResize.resize(capacity, targetSize);
        if (newCapacity <= capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);
        if (targetSize > newCapacity) {
            throw new IndexOutOfBoundsException("Buffer is full below the target size: " + newCapacity + " -> " + targetSize);
        }
        resize(newCapacity);
        return true;
    }

    @Override
    public void trim() {
        final long readableBytes = readableBytes();
        if (readableBytes == capacity()) return;
        final Arena arena = this.arenaSupplier.get();
        final MemorySegment oldSegment = this.segment;
        final MemorySegment segment = NetworkBufferSegmentAllocator.allocate(arena, readableBytes);
        MemorySegment.copy(oldSegment, readIndex(), segment, 0, readableBytes);
        this.segment = segment;
        this.arena = arena;
        this.writeIndex(readableBytes);
        this.readIndex(0);
    }
}
