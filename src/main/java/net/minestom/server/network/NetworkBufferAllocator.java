package net.minestom.server.network;

import net.minestom.server.ServerFlag;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Reference;
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
@ApiStatus.Internal
final class NetworkBufferAllocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkBufferAllocator.class);
    // true if we use system malloc and free.
    private static final boolean ENABLE_NATIVE = ServerFlag.FORCE_NATIVE_ALLOCATION
            || (ServerFlag.ATTEMPT_NATIVE_ALLOCATION && NetworkBufferAllocator.class.getModule().isNativeAccessEnabled());

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
            SEGMENT_CLEANER = NetworkBufferAllocator::free;
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
    public static MemorySegment allocate(Arena arena, long byteSize) {
        Objects.requireNonNull(arena, "arena");
        if (!ENABLE_NATIVE) {
            // Fall back to regular implementation, we check for not nullness, as Arena is implementable
            return Objects.requireNonNull(arena.allocate(byteSize), "segment");
        }
        // We can use native implementation, which involves checking bounds early.
        Check.argCondition(byteSize < 0, "Cannot allocate a negative size found {}", byteSize);
        final MemorySegment segment;
        try {
            segment = (MemorySegment) MALLOC_HANDLE.invokeExact(byteSize);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to allocate native memory: %d".formatted(byteSize), e);
        }
        // malloc mostly returns NULL when byteSize > 0 for OOM.
        if (byteSize > 0 && segment.address() == MemorySegment.NULL.address())
            throw new OutOfMemoryError("Failed to allocate native memory: %d".formatted(byteSize));

        try {
            return segment.reinterpret(byteSize, arena, SEGMENT_CLEANER);
        } catch (Exception e) {
            // We need to attempt to clean if it failed to reinterpret.
            // This might cause another exception to bubble up which you wouldn't see the initial error.
            NetworkBufferAllocator.free(segment);
            throw new IllegalStateException("Failed to reinterpret native memory: %d:%d".formatted(segment.address(), byteSize), e);
        } finally {
            // We need a reachability fence to ensure reinterpret can be called somewhat safely for global auto arenas.
            Reference.reachabilityFence(arena);
        }
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
