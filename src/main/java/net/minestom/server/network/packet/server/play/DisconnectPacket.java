package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DisconnectPacket implements ServerPacket {
    public String message;

    /**
     * @deprecated Use {@link #message}
     */
    @Deprecated public JsonMessage messageJson;

    /**
     * Creates a new disconnect packet with a given string.
     * @param message the message
     */
    public DisconnectPacket(@NotNull String message) {
        this.message = message;
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
        writer.writeSizedString(messageJson != null ? messageJson.toString() : message);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
