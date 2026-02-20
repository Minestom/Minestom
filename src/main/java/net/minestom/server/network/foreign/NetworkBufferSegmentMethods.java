package net.minestom.server.network.foreign;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.Charset;

// Small helper class for JDK updates that we can use for extra performance.
// Patches:
// - JDK:8369564 https://github.com/openjdk/jdk/pull/28043 (JDK 27)
// - JDK:8372353 https://github.com/openjdk/jdk/pull/28454 (JDK XX)
@SuppressWarnings("all") // Idea warns about usage of MethodHandles.
final class NetworkBufferSegmentMethods {
    // static long copy(String src, Charset dstEncoding, int srcIndex, MemorySegment dst, long dstOffset, int numChars)
    private static final @Nullable MethodHandle SEGMENENT_COPY = segmentCopy();
    // String getString(long offset, Charset charset, long byteLength);
    private static final @Nullable MethodHandle SEGMENT_STRING = segmentString();
    // int int getBytesLength(Charset cs)
    public static final @Nullable MethodHandle STRING_BYTES_LENGTH = stringBytesLength();

    static final boolean STRING_SUPPORTED = SEGMENENT_COPY != null && SEGMENT_STRING != null /* && STRING_BYTES_LENGTH != null */;

    static String getString(MemorySegment segment, long offset, Charset charset, long byteLength) {
        assert STRING_SUPPORTED : "MemorySegment.getString() not found";
        try {
            return (String) SEGMENT_STRING.invokeExact(segment, offset, charset, byteLength);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get string from MemorySegment", t);
        }
    }

    static void copy(String src, Charset dstEncoding, int srcIndex, MemorySegment dst, long dstOffset, int numChars) {
        assert STRING_SUPPORTED : "MemorySegment.copy() not found";
        try {
            SEGMENENT_COPY.invokeExact(src, dstEncoding, srcIndex, dst, dstOffset, numChars);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to copy string to MemorySegment", t);
        }
    }

    private static @Nullable MethodHandle segmentCopy() {
        try {
            return MethodHandles.publicLookup().findStatic(MemorySegment.class, "copy", MethodType.methodType(long.class, String.class, Charset.class, int.class, MemorySegment.class, long.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException _) {
            return null;
        }
    }

    private static @Nullable MethodHandle segmentString() {
        try {
            return MethodHandles.publicLookup().findVirtual(MemorySegment.class, "getString", MethodType.methodType(String.class, long.class, Charset.class, long.class));
        } catch (NoSuchMethodException | IllegalAccessException _) {
            return null;
        }
    }

    private static @Nullable MethodHandle stringBytesLength() {
        try {
            return MethodHandles.publicLookup().findVirtual(String.class, "getBytesLength", MethodType.methodType(int.class, Charset.class));
        } catch (NoSuchMethodException | IllegalAccessException _) {
            return null;
        }
    }
}
