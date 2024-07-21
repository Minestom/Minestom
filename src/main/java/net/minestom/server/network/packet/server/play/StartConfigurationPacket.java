package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record StartConfigurationPacket() implements ServerPacket.Play {
    public StartConfigurationPacket(NetworkBuffer reader) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

}
