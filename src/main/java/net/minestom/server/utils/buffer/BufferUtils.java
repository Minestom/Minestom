package net.minestom.server.utils.buffer;

import com.github.pbbl.heap.ByteBufferPool;

import java.nio.ByteBuffer;

public class BufferUtils {

    private static ByteBufferPool pool = new ByteBufferPool();

    public static BufferWrapper getBuffer(int size) {
        return new BufferWrapper(pool.take(size));
    }

    protected static void giveBuffer(ByteBuffer byteBuffer) {
        pool.give(byteBuffer);
    }

}
