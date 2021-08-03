package net.minestom.server.network.netty.packet;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
public class FramedPacket {

    private final ByteBuffer body;

    public FramedPacket(@NotNull ByteBuffer body) {
        this.body = body;
    }

    public @NotNull ByteBuffer getBody() {
        return body;
    }
}
