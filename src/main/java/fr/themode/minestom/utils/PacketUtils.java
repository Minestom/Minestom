package fr.themode.minestom.utils;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

import java.util.function.Consumer;

public class PacketUtils {

    public static void writePacket(ServerPacket serverPacket, Consumer<Packet> callback) {
        int id = serverPacket.getId();
        Packet packet = Packet.builder();
        Utils.writeVarInt(packet, id);
        PacketWriter packetWriter = new PacketWriter(packet);
        serverPacket.write(packetWriter);

        System.out.println("WRITE PACKET: " + id + " " + serverPacket.getClass().getSimpleName());

        callback.accept(packet.prepend(p -> {
            Utils.writeVarInt(packet, packet.getSize());
        }));
    }

}
