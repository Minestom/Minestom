package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class BufUtils {

	private static final PooledByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

	public static ByteBuf getBuffer() {
		return alloc.heapBuffer();
	}

	public static ByteBuf getBuffer(boolean io) {
		return io ? alloc.ioBuffer() : alloc.heapBuffer();
	}

	public static ByteBuf getBuffer(int initialCapacity) {
		return alloc.heapBuffer(initialCapacity);
	}

	public static ByteBuf getBuffer(boolean io, int initialCapacity) {
		return io ? alloc.ioBuffer(initialCapacity) : alloc.heapBuffer(initialCapacity);
	}

	public static ByteBuf getBuffer(int initialCapacity, int maxCapacity) {
		return alloc.heapBuffer(initialCapacity, maxCapacity);
	}

	public static ByteBuf getBuffer(boolean io, int initialCapacity, int maxCapacity) {
		return io ? alloc.ioBuffer(initialCapacity, maxCapacity) : alloc.heapBuffer(initialCapacity, maxCapacity);
	}

}
