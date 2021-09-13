package net.minestom.server.utils.binary;

import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class PooledBuffers {
    private final static Queue<SoftReference<BinaryBuffer>> POOLED_BUFFERS = new ConcurrentLinkedQueue<>();
    private final static int BUFFER_SIZE = 262_143;

    public static BinaryBuffer get() {
        BinaryBuffer buffer = null;
        SoftReference<BinaryBuffer> ref;
        while ((ref = POOLED_BUFFERS.poll()) != null) {
            buffer = ref.get();
            if (buffer != null) break;
        }
        return Objects.requireNonNullElseGet(buffer, () -> BinaryBuffer.ofSize(BUFFER_SIZE));
    }

    public static void add(BinaryBuffer buffer) {
        buffer.clear();
        POOLED_BUFFERS.add(new SoftReference<>(buffer));
    }

    public static int bufferSize() {
        return BUFFER_SIZE;
    }
}
