package net.minestom.server.utils.buffer;

import com.github.pbbl.heap.ByteBufferPool;

import java.nio.ByteBuffer;

public final class BufferUtils {

    private static final ByteBufferPool pool = new ByteBufferPool();

    private BufferUtils() {

    }

    public static BufferWrapper getBuffer(int size) {
        return new BufferWrapper(pool.take(size));
    }

    protected static void giveBuffer(ByteBuffer byteBuffer) {
        pool.give(byteBuffer);
    }
}
