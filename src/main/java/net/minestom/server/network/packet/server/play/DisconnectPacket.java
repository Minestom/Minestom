package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DisconnectPacket implements ServerPacket {
    private String payload;

    /**
     * Creates a new disconnect packet with a given string.
     * @param payload the message
     */
    public DisconnectPacket(@NotNull String payload) {
        this.payload = payload;
    }

    /**
     * @deprecated Use {@link #DisconnectPacket(String)}
     */
    @Deprecated
    public DisconnectPacket(@NotNull JsonMessage message){
        this(message.toString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(payload);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
