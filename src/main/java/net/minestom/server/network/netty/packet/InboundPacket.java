package net.minestom.server.network.netty.packet;

import io.netty.buffer.ByteBuf;

public class InboundPacket {

    public final int packetId;
    public final ByteBuf body;

    public InboundPacket(int id, ByteBuf body) {
        this.packetId = id;
        this.body = body;
    }

}
