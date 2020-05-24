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

        //System.out.println("WRITE PACKET: " + id + " " + serverPacket.getClass().getSimpleName());

        return Unpooled.copiedBuffer(buffer);
    }

}
