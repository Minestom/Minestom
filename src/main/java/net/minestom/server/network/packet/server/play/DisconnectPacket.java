package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class DisconnectPacket implements ServerPacket {

    public JsonMessage message; // Only text

    @Override
    public void write(BinaryWriter writer) {
        writer.writeSizedString(message.toString());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
