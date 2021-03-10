package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DisconnectPacket implements ServerPacket {
    public Component message;

    /**
     * Creates a new disconnect packet with a given message.
     * @param message the message
     */
    public DisconnectPacket(@NotNull Component message) {
        this.message = message;
    }

    /**
     * @deprecated Use {@link #DisconnectPacket(Component)}
     */
    @Deprecated
    public DisconnectPacket(@NotNull JsonMessage message){
        this(message.asComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(message);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISCONNECT;
    }
}
