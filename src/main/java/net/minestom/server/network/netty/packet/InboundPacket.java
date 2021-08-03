package net.minestom.server.network.netty.packet;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class InboundPacket {
    private final int packetId;
    private final ByteBuffer body;

    public InboundPacket(int id, @NotNull ByteBuffer body) {
        this.packetId = id;
        this.body = body;
    }

    public int getPacketId() {
        return packetId;
    }

    public @NotNull ByteBuffer getBody() {
        return body;
    }
}
