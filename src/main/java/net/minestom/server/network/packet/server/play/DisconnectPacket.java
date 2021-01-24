package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DisconnectPacket implements ServerPacket {

    public JsonMessage message; // Only text

    public DisconnectPacket(@NotNull JsonMessage message){
        this.message = message;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message.toString());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
