package net.minestom.server.utils.cache;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.TimeUnit;

public class TemporaryPacketCache extends TemporaryCache<TimedBuffer> {
    public TemporaryPacketCache(long duration, TimeUnit timeUnit) {
        super(duration, timeUnit, (key, value, cause) -> {
            if (value == null)
                return;
            final ByteBuf buffer = value.getBuffer();
            synchronized (buffer) {
                buffer.release();
            }
        });
    }
}