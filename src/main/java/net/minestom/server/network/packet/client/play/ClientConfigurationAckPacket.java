package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientConfigurationAckPacket() implements ClientPacket {

    public ClientConfigurationAckPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public @NotNull ConnectionState nextState() {
        return ConnectionState.CONFIGURATION;
    }
}
