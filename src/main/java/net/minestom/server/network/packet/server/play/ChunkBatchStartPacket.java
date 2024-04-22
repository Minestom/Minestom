package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record ChunkBatchStartPacket() implements ServerPacket.Play {
    public ChunkBatchStartPacket(@NotNull NetworkBuffer buffer) {
        this();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.CHUNK_BATCH_START;
    }
}
