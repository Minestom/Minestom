package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record MultiBlockChangePacket(long chunkSectionPosition, long[] blocks) implements ServerPacket {
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

    @Override
    public int getId() {
        return ServerPacketIdentifier.MULTI_BLOCK_CHANGE;
    }
}
