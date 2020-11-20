package net.minestom.server.utils.cache;

import io.netty.buffer.ByteBuf;

/**
 * Convenient superclass of {@link TemporaryCache} explicitly for packet to store a {@link ByteBuf}.
 */
public class TemporaryPacketCache extends TemporaryCache<ByteBuf> {
    public TemporaryPacketCache(long keepTime) {
        super(keepTime);
    }
}
