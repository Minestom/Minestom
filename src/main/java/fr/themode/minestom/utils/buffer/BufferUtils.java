package fr.themode.minestom.utils.buffer;

import pbbl.heap.HeapByteBufferPool;

import java.nio.ByteBuffer;

public class BufferUtils {

    private static HeapByteBufferPool pool = new HeapByteBufferPool();

    public static BufferWrapper getBuffer(int size) {
        return new BufferWrapper(pool.take(size));
    }

    protected static void giveBuffer(ByteBuffer byteBuffer) {
        pool.give(byteBuffer);
    }

}
