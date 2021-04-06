package net.minestom.server.network.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minestom.server.network.packet.Packet;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) {
        PacketUtils.writePacket(buf, packet);
    }

}
