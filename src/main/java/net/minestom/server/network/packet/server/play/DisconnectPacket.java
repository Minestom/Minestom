package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class DisconnectPacket implements ServerPacket {

    public ColoredText message;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(message.toString());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
