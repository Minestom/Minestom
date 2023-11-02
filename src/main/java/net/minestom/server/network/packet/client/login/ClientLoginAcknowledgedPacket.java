package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientLoginAcknowledgedPacket() implements ClientPacket {

    public ClientLoginAcknowledgedPacket(@NotNull NetworkBuffer buffer) {
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
