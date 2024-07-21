package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkDataPacket(int chunkX, int chunkZ,
                              @NotNull ChunkData chunkData,
                              @NotNull LightData lightData) implements ServerPacket.Play {
    public ChunkDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(INT),
                new ChunkData(reader),
                new LightData(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, chunkX);
        writer.write(INT, chunkZ);
        writer.write(chunkData);
        writer.write(lightData);
    }

}