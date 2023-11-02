package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FinishConfigurationPacket() implements ServerPacket {

    public FinishConfigurationPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return ServerPacketIdentifier.CONFIGURATION_FINISH_CONFIGURATION;
    }

    @Override
    public @NotNull ConnectionState nextState() {
        return ConnectionState.PLAY;
    }
}
