package net.minestom.server.network.packet.server.login;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LoginDisconnectPacket implements ServerPacket {

    private final String kickMessage; // JSON text

    public LoginDisconnectPacket(@NotNull String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public LoginDisconnectPacket(@NotNull JsonMessage jsonKickMessage) {
        this(jsonKickMessage.toString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(kickMessage);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_DISCONNECT;
    }

}
