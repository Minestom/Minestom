package net.minestom.server.network.foreign;

import net.minestom.server.network.NetworkBufferAllocator.ArenaStrategy;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Represents {@link #resizableBuffer(long, Registries)}.
 * <br>
 * Which are the most commonly used when the bounds of the application are unknown for example {@link #makeArray(Type, Object)}.
 * These will be converted into {@link NetworkBufferStaticSegmentImpl} if applicable, for example {@link #readOnly()}.
 */
@ApiStatus.Internal
final class NetworkBufferResizeableSegmentImpl extends NetworkBufferSegmentImpl {
    private final AutoResize autoResize;
    private final ArenaStrategy arenaStrategy;

    private Arena arena;
    private MemorySegment segment;

    NetworkBufferResizeableSegmentImpl(Arena arena, MemorySegment segment, long readIndex, long writeIndex, AutoResize autoResize, ArenaStrategy arenaStrategy, @Nullable Registries registries) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.autoResize = Objects.requireNonNull(autoResize, "autoResize");
        this.arenaStrategy = Objects.requireNonNull(arenaStrategy, "arenaStrategy");
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
        reallocate(length, capacity, 0);
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
        reallocate(readableBytes, readableBytes, readIndex());
        this.writeIndex(readableBytes);
        this.readIndex(0);
    }

    private void reallocate(long length, long capacity, long target) {
        final Arena oldArena = this.arena;
        final MemorySegment oldSegment = this.segment;
        final ArenaStrategy arenaStrategy = this.arenaStrategy;
        final Arena arena = Objects.requireNonNull(arenaStrategy.acquire(), "arena");
        final MemorySegment segment = NetworkBufferNativeSegmentAllocator.allocate(arena, length);
        MemorySegment.copy(oldSegment, target, segment, 0, capacity);
        this.segment = segment;
        this.arena = arena;
        arenaStrategy.release(oldArena);
    }
}
