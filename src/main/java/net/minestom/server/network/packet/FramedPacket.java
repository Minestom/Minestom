package net.minestom.server.network.packet;

import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
@ApiStatus.Internal
public final class FramedPacket {
    private final int packetId;
    private final ByteBuffer body;
    private final ServerPacket packet;

    public FramedPacket(int packetId, @NotNull ByteBuffer body, @NotNull ServerPacket packet) {
        this.packetId = packetId;
        this.body = body;
        this.packet = packet;
    }

    public int packetId() {
        return packetId;
    }

    public @NotNull ByteBuffer body() {
        return body;
    }

    public @NotNull ServerPacket packet() {
        return packet;
    }
}
