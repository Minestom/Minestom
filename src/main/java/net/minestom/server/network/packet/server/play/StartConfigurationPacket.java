package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StartConfigurationPacket() implements ServerPacket {

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return ServerPacketIdentifier.START_CONFIGURATION_PACKET;
    }

    @Override
    public @NotNull ConnectionState nextState() {
        return ConnectionState.CONFIGURATION;
    }
}
