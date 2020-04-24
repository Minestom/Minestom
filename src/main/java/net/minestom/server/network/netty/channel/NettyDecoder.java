package net.minestom.server.network.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minestom.server.network.netty.packet.PacketHandler;
import net.minestom.server.utils.Utils;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

    private int bytesToRead;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {

        // Fix cut packet
        if (bytesToRead != 0) {
            int readable = buffer.readableBytes();
            if (readable >= bytesToRead) {
                PacketHandler packetHandler = new PacketHandler();
                packetHandler.length = bytesToRead;
                packetHandler.buffer = buffer.readBytes(bytesToRead);
                out.add(packetHandler);
                bytesToRead = 0;
            }
            return;
        }

        int packetLength = Utils.readVarInt(buffer);
        PacketHandler packetHandler = new PacketHandler();
        packetHandler.length = packetLength;
        if (packetLength == 0xFE) { // Legacy server ping
            packetHandler.buffer = buffer.readBytes(2);
        } else {
            int readable = buffer.readableBytes();
            if (readable < packetLength) {
                // Wait for bytes to arrive
                bytesToRead = packetLength;
                return;
            } else {
                // There are enough bytes, read them
                packetHandler.buffer = buffer.readBytes(packetLength);
                bytesToRead = 0;
            }
        }

        out.add(packetHandler);
    }
}
