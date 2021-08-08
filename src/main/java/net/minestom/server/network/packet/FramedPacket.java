package net.minestom.server.network.packet;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
public final class FramedPacket {
    private final int packetId;
    private final ByteBuffer body;

    public FramedPacket(int packetId, @NotNull ByteBuffer body) {
        this.packetId = packetId;
        this.body = body;
    }

    public int packetId() {
        return packetId;
    }

    public @NotNull ByteBuffer body() {
        return body;
    }
}
