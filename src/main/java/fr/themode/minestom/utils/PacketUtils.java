package fr.themode.minestom.utils;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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

        //System.out.println("WRITE PACKET: " + id + " " + serverPacket.getClass().getSimpleName() + " size: " + length);

        return Unpooled.copiedBuffer(buffer);
    }

}
