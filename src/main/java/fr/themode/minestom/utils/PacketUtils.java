package fr.themode.minestom.utils;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import simplenet.packet.Packet;

import java.util.function.Consumer;

public class PacketUtils {

    public static void writePacket(ServerPacket serverPacket, Consumer<Packet> callback) {
        int id = serverPacket.getId();
        //System.out.println("SEND PACKET: 0x"+Integer.toHexString(id));
        Packet packet = Packet.builder();
        Utils.writeVarInt(packet, id);
        PacketWriter packetWriter = new PacketWriter(packet);
        serverPacket.write(packetWriter);

        callback.accept(packet.prepend(p -> {
            Utils.writeVarInt(packet, packet.getSize());
        }));
    }

}
