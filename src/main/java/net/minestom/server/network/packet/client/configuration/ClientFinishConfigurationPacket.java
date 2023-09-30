package net.minestom.server.network.packet.client.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientFinishConfigurationPacket() implements ClientPacket {

    public ClientFinishConfigurationPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

}
