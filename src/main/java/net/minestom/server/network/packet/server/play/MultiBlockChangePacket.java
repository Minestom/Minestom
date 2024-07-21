package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.VAR_LONG_ARRAY;

public record MultiBlockChangePacket(long chunkSectionPosition, long[] blocks) implements ServerPacket.Play {
    public MultiBlockChangePacket(int chunkX, int section, int chunkZ,
                                  long[] blocks) {
        this(((long) (chunkX & 0x3FFFFF) << 42) | (section & 0xFFFFF) | ((long) (chunkZ & 0x3FFFFF) << 20), blocks);
    }

    public MultiBlockChangePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG), reader.read(VAR_LONG_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, chunkSectionPosition);
        writer.write(VAR_LONG_ARRAY, blocks);
    }

}
