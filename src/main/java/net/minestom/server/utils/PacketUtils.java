package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public class PacketUtils {

    public static ByteBuf writePacket(ServerPacket serverPacket) {
        int id = serverPacket.getId();
        PacketWriter packetWriter = new PacketWriter();

        packetWriter.writeVarInt(id);

        serverPacket.write(packetWriter);

        byte[] bytes = packetWriter.toByteArray();
        int length = bytes.length;

        int varIntSize = Utils.lengthVarInt(length);

        ByteBuf buffer = Unpooled.buffer(length + varIntSize);
        Utils.writeVarIntBuf(buffer, length);
        buffer.writeBytes(bytes);

        //if(!(serverPacket instanceof ChunkDataPacket) && !(serverPacket instanceof PlayerListHeaderAndFooterPacket))
        //System.out.println("WRITE PACKET: " + serverPacket.getClass().getSimpleName());

        //Unpooled.copiedBuffer(buffer);
        return buffer;
    }

}
