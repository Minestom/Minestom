package net.minestom.server.network.foreign;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Allocator for {@link NetworkBuffer} segments that attempts to use native memory segments via
 * {@code malloc} and {@code free} instead of the default {@link Arena} allocator.
 *
 * <p>
 * The default {@link Arena} implementation often uses a modified {@code calloc}
 * approach which zeros all allocated memory for safety. This bypasses that zeroing
 * by using {@code malloc} (which does not zero memory),
 * as zeroing is unnecessary for {@link NetworkBuffer} use case, providing a performance benefit.
 * </p>
 * <p>
 * This custom native allocation is not forcefully enabled by default, unless:
 * <ul>
 * <li>When permitted by {@link Module#isNativeAccessEnabled()} behind
 * ({@link ServerFlag#ATTEMPT_NATIVE_ALLOCATION}).</li>
 * <li>When explicitly requested by the user
 * ({@link ServerFlag#FORCE_NATIVE_ALLOCATION}).</li>
 * </ul>
 */
public final class NetworkBufferSegmentAllocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkBufferSegmentAllocator.class);
    // true if we use system malloc and free.
    public static final boolean ENABLE_NATIVE = ServerFlag.FORCE_NATIVE_ALLOCATION
            || (ServerFlag.ATTEMPT_NATIVE_ALLOCATION && NetworkBufferSegmentAllocator.class.getModule().isNativeAccessEnabled());

    // malloc(size_t size) -> void*
    private static final @UnknownNullability MethodHandle MALLOC_HANDLE;
    // free(void* ptr) -> void
    private static final @UnknownNullability MethodHandle FREE_HANDLE;
    // Cleaner to call free, use a global instance instead of allocating a new lambda
    private static final @UnknownNullability Consumer<MemorySegment> SEGMENT_CLEANER;

    static {
        if (ENABLE_NATIVE) {
            MALLOC_HANDLE = Linker.nativeLinker().downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("malloc").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
            );
            FREE_HANDLE = Linker.nativeLinker().downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("free").orElseThrow(),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            );
            SEGMENT_CLEANER = NetworkBufferSegmentAllocator::free;
            LOGGER.info("Using native malloc/free implementation for NetworkBuffer allocations.");
        } else {
            MALLOC_HANDLE = null;
            FREE_HANDLE = null;
            SEGMENT_CLEANER = null;
        }
    }

    /**
     * Allocates a new {@link MemorySegment} using the arena using native implementations if available.
     *
     * @param arena the arena to use
     * @param byteSize the byte size.
     * @throws NullPointerException for any null arena or segment
     * @throws IllegalArgumentException if {@code byteSize} is less than 0.
     * @throws OutOfMemoryError when native and {@code byteSize} is greater than zero and {@link MemorySegment#NULL} is returned.
     * @throws IllegalStateException if memory fails to be allocated or reinterpreted
     * @return the new {@link MemorySegment} with size {@code byteSize} for {@code arena}
     */
    @ApiStatus.Internal
    static MemorySegment allocate(Arena arena, long byteSize) {
        Objects.requireNonNull(arena, "arena");
        if (!ENABLE_NATIVE) {
            // Fall back to regular implementation, we check for not nullness, as Arena is implementable
            final MemorySegment segment = arena.allocate(byteSize);
            Objects.requireNonNull(segment, "segment");
            assert !segment.isReadOnly() : "Allocated segment is read-only: %d:%d".formatted(segment.address(), byteSize);
            return segment;
        }
        // We can use native implementation.
        final MemorySegment segment = malloc(byteSize);
        try {
            return segment.reinterpret(byteSize, arena, SEGMENT_CLEANER);
        } catch (RuntimeException e) {
            // We need to attempt to clean if it failed to reinterpret.
            try {
                throw new IllegalStateException("Failed to reinterpret native memory: %d:%d".formatted(segment.address(), byteSize), e);
            } finally {
                // Attempt to free could cause another exception.
                NetworkBufferSegmentAllocator.free(segment);
            }
        }
    }

    private static MemorySegment malloc(long byteSize) {
        if (byteSize < 0) throw new IllegalArgumentException("Cannot allocate a negative size found %d".formatted(byteSize));
        if (!ENABLE_NATIVE) throw new IllegalStateException("Cannot malloc without native access.");
        final MemorySegment segment;
        try {
            segment = (MemorySegment) MALLOC_HANDLE.invokeExact(byteSize);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to allocate native memory: %d".formatted(byteSize), e);
        }
        // malloc mostly returns NULL when byteSize > 0 for OOM.
        if (byteSize > 0 && segment.address() == MemorySegment.NULL.address())
            throw new OutOfMemoryError("Failed to allocate native memory: %d".formatted(byteSize));
        return segment;
    }

    private static void free(MemorySegment segment) {
        Objects.requireNonNull(segment, "segment");
        if (!ENABLE_NATIVE) throw new IllegalStateException("Cannot free without native access.");
        try {
            FREE_HANDLE.invokeExact(segment);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to free native memory: %d:%d".formatted(segment.address(), segment.byteSize()), e);
        }
    }
}
