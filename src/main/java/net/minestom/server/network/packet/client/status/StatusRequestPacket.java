package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record StatusRequestPacket() implements ClientPacket {
    public StatusRequestPacket(@NotNull NetworkBuffer reader) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        // Empty
    }
}
