package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkBiomeData(int chunkX, int chunkZ, byte @NotNull [] data) implements NetworkBuffer.Writer {

    public ChunkBiomeData(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(INT), reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, chunkX);
        writer.write(INT, chunkZ);
        writer.write(BYTE_ARRAY, data);
    }
}
