package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StartConfigurationPacket() implements ServerPacket {

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.START_CONFIGURATION_PACKET;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

}
