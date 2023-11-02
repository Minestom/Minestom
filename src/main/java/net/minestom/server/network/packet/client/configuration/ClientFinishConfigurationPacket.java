package net.minestom.server.network.packet.client.configuration;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientFinishConfigurationPacket() implements ClientPacket {

    public ClientFinishConfigurationPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public @NotNull ConnectionState nextState() {
        return ConnectionState.PLAY;
    }
}
