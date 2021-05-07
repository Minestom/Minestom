package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class BufUtils {

    private static final PooledByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

    public static ByteBuf direct() {
        return alloc.ioBuffer();
    }
}
