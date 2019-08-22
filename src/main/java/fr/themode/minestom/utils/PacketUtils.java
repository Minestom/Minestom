package fr.themode.minestom.utils;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.net.packet.server.ServerPacket;

import static fr.themode.minestom.net.protocol.MinecraftProtocol.PACKET_ID_IDENTIFIER;

public class PacketUtils {

    public static Packet writePacket(ServerPacket serverPacket) {
        int id = serverPacket.getId();
        Packet packet = Packet.create();
        Buffer buffer = packet.getPayload();
        serverPacket.write(buffer);
        /*if (id != 40 && id != 64)
            System.out.println("ID: 0x" + Integer.toHexString(id));*/
        packet.put(PACKET_ID_IDENTIFIER, id);
        return packet;
    }

}
