package fr.themode.minestom.utils;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

import static fr.themode.minestom.net.protocol.MinecraftProtocol.PACKET_ID_IDENTIFIER;
import static fr.themode.minestom.utils.Utils.writeVarInt;

public class PacketUtils {

    public static Packet writePacket(ServerPacket serverPacket) {
        int id = serverPacket.getId();
        Packet packet = Packet.create();
        Buffer buffer = packet.getPayload();

        PacketWriter packetWriter = new PacketWriter(buffer);

        serverPacket.write(packetWriter);
        packet.put(PACKET_ID_IDENTIFIER, id);
        return packet;
    }

    public static boolean verify(Packet packet) {
        return packet.get("id") != null;
    }

    public static Buffer encode(Packet packet) {
        Buffer buffer = Buffer.create();
        Buffer idAndPayload = Buffer.create();

        writeVarInt(idAndPayload, packet.get(PACKET_ID_IDENTIFIER));
        idAndPayload.putBuffer(packet.getPayload());
        writeVarInt(buffer, idAndPayload.length());
        buffer.putBuffer(idAndPayload);
        return buffer;
    }

}
