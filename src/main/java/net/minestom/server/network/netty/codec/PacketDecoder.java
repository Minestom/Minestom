package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.utils.Utils;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    private final String owner;

    public PacketDecoder(String owner) {
        this.owner = owner;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        if (buf.readableBytes() > 0) {
            final int packetId = Utils.readVarInt(buf);
            list.add(new InboundPacket(packetId, buf));
        }
    }

    @Override
    public String toString() {
        return "PacketDecoder("+owner+")";
    }
}
