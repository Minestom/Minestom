package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class DisconnectPacket implements ServerPacket {

    public String message;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(message);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
