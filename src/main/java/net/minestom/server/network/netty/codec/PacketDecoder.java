package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minestom.server.network.netty.packet.InboundPacket;
import net.minestom.server.utils.Utils;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (buf.readableBytes() > 0) {
            list.add(new InboundPacket(Utils.readVarInt(buf), buf));
        }
    }
}
