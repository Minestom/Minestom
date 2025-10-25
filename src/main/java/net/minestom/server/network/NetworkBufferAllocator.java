package net.minestom.server.network;

import net.minestom.server.ServerFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Allocator for {@link NetworkBuffer} segments that attempts to use native memory segments via
 * {@code malloc} and {@code free} instead of the default {@link java.lang.foreign.Arena} allocator.
 *
 * <p>
 * The default {@link java.lang.foreign.Arena} implementation often uses a modified {@code calloc}
 * approach which zeros all allocated memory for safety. This bypasses that zeroing
 * by using {@code malloc} (which does not zero memory),
 * as zeroing is unnecessary for {@link NetworkBuffer} use case, providing a performance benefit.
 * </p>
 *
 * This custom native allocation is not forcefully enabled by default, unless:
 * <ul>
 * <li>When permitted by {@link Module#isNativeAccessEnabled()} behind
 * ({@link net.minestom.server.ServerFlag#ATTEMPT_NATIVE_ALLOCATION}).</li>
 * <li>When explicitly requested by the user
 * ({@link net.minestom.server.ServerFlag#FORCE_NATIVE_ALLOCATION}).</li>
 * </ul>
 */
@ApiStatus.Internal
final class NetworkBufferAllocator {
    // true if we use system malloc and free.
    private static final boolean ENABLE_NATIVE = ServerFlag.FORCE_NATIVE_ALLOCATION
            || (ServerFlag.ATTEMPT_NATIVE_ALLOCATION && NetworkBufferAllocator.class.getModule().isNativeAccessEnabled());

    // malloc(size_t size) -> void*
    private static final @UnknownNullability MethodHandle MALLOC_HANDLE;
    // free(void* ptr) -> void
    private static final @UnknownNullability MethodHandle FREE_HANDLE;

    static {
        final MethodHandle mallocHandle;
        final MethodHandle freeHandle;
        if (ENABLE_NATIVE) {
            mallocHandle = Linker.nativeLinker().downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("malloc").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
            );
            freeHandle = Linker.nativeLinker().downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("free").orElseThrow(),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            );
        } else {
            mallocHandle = null;
            freeHandle = null;
        }
        MALLOC_HANDLE = mallocHandle;
        FREE_HANDLE = freeHandle;
    }

    private NetworkBufferAllocator() {
    }

    public static MemorySegment allocate(Arena arena, long byteSize) {
        if (!ENABLE_NATIVE) return arena.allocate(byteSize);
        Objects.requireNonNull(arena, "arena");
        try {
            MemorySegment segment = (MemorySegment) MALLOC_HANDLE.invokeExact(byteSize);
            if (segment.address() == MemorySegment.NULL.address())
                throw new OutOfMemoryError("Native failed to allocate: %d".formatted(byteSize));
            return segment.reinterpret(byteSize, arena, NetworkBufferAllocator::cleaner);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to allocate native memory", e);
        }
    }

    private static void cleaner(MemorySegment segment) {
        if (!ENABLE_NATIVE) throw new IllegalStateException("Cannot clean native memory");
        Objects.requireNonNull(segment, "segment");
        try {
            FREE_HANDLE.invokeExact(segment);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to free native memory", e);
        }
    }
}
