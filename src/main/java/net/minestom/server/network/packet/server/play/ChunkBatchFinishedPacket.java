package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ChunkBatchFinishedPacket(int batchSize) implements ServerPacket {

    public ChunkBatchFinishedPacket(@NotNull NetworkBuffer buffer) {
        this(buffer.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, batchSize);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return ServerPacketIdentifier.CHUNK_BATCH_FINISHED;
    }
}
