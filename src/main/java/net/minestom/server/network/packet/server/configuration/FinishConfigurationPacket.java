package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

public record FinishConfigurationPacket() implements ServerPacket {

    public FinishConfigurationPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_FINISH_CONFIGURATION;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION);
        };
    }
}
