package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record BundlePacket() implements ServerPacket.Play {
    public BundlePacket(@NotNull NetworkBuffer reader) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

}
