package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record ChunkBatchStartPacket() implements ServerPacket.Play {
    public ChunkBatchStartPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

}
