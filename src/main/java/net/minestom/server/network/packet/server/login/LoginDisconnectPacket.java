package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LoginDisconnectPacket implements ServerPacket {

    private final Component kickMessage; // JSON text

    public LoginDisconnectPacket(@NotNull Component kickMessage) {
        this.kickMessage = kickMessage;
    }

    /**
     * @deprecated Use {@link #LoginDisconnectPacket(Component)}
     */
    @Deprecated
    public LoginDisconnectPacket(@NotNull JsonMessage jsonKickMessage) {
        this(jsonKickMessage.asComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(kickMessage);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_DISCONNECT;
    }

}
