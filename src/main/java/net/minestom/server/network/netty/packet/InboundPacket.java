package net.minestom.server.network.netty.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class InboundPacket {

    public final int packetId;
    public final ByteBuf body;

    public InboundPacket(int id, @NotNull ByteBuf body) {
        this.packetId = id;
        this.body = body;
    }

}
