package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record MultiBlockChangePacket(long chunkSectionPosition,
                                     boolean suppressLightUpdates,
                                     long[] blocks) implements ServerPacket {
    public MultiBlockChangePacket(int chunkX, int section, int chunkZ,
                                  boolean suppressLightUpdates,
                                  long[] blocks) {
        this(((long) (chunkX & 0x3FFFFF) << 42) | (section & 0xFFFFF) | ((long) (chunkZ & 0x3FFFFF) << 20),
                suppressLightUpdates, blocks);
    }

    public MultiBlockChangePacket(BinaryReader reader) {
        this(reader.readLong(), reader.readBoolean(), reader.readVarLongArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(chunkSectionPosition);
        writer.writeBoolean(suppressLightUpdates);
        writer.writeVarLongArray(blocks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MULTI_BLOCK_CHANGE;
    }
}
