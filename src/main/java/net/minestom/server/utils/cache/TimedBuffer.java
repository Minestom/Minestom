package net.minestom.server.utils.cache;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Object containing a {@link ByteBuf buffer} and its timestamp.
 * Used for packet-caching to use the most recent.
 */
public class TimedBuffer {

    private final ByteBuf buffer;
    private final long timestamp;

    public TimedBuffer(@NotNull ByteBuf buffer, long timestamp) {
        this.buffer = buffer;
        this.timestamp = timestamp;
    }

    @NotNull
    public ByteBuf getBuffer() {
        return buffer;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
