package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChunkBatchReceivedPacket(float targetChunksPerTick) implements ClientPacket {

    public ClientChunkBatchReceivedPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, targetChunksPerTick);
    }
}
