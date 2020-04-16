package fr.themode.minestom.net.netty.channel;

import fr.themode.minestom.net.netty.packet.PacketHandler;
import fr.themode.minestom.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        int packetLength = Utils.readVarInt(buffer);
        PacketHandler packetHandler = new PacketHandler();
        packetHandler.length = packetLength;
        if (packetLength == 0xFE) { // Legacy server ping
            packetHandler.buffer = buffer.readBytes(2);
        } else {
            packetHandler.buffer = buffer.readBytes(packetLength);
        }

        out.add(packetHandler);
    }
}
