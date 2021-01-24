package net.minestom.server.network.netty.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class InboundPacket {

    private final int packetId;
    private final ByteBuf body;

    public InboundPacket(int id, @NotNull ByteBuf body) {
        this.packetId = id;
        this.body = body;
    }

    public int getPacketId() {
        return packetId;
    }

    @NotNull
    public ByteBuf getBody() {
        return body;
    }
}
