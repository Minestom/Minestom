package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minestom.server.network.netty.packet.FramedPacket;

public class GroupedPacketHandler extends MessageToByteEncoder<FramedPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FramedPacket msg, ByteBuf out) {
        final ByteBuf packet = msg.body;
        out.writeBytes(packet.duplicate());
        if (msg.releaseBuf) {
            packet.release();
        }
    }
}
