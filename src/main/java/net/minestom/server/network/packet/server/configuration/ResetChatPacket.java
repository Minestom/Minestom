package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record ResetChatPacket() implements ServerPacket.Configuration {

    public ResetChatPacket(@NotNull NetworkBuffer reader) {
        this(); // No fields
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        // No fields
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_RESET_CHAT;
    }
}
