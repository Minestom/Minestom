package net.minestom.server.network.packet.server.play;

import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class MultiBlockChangePacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;
    public int section;
    //TODO this is important prob if we add a light api
    public boolean suppressLightUpdates = true;
    public BlockChange[] blockChanges = new BlockChange[0];

    public MultiBlockChangePacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(ChunkUtils.getChunkIndexWithSection(chunkX, chunkZ, section));
        writer.writeBoolean(suppressLightUpdates);
        if (blockChanges != null) {
            final int length = blockChanges.length;
            writer.writeVarInt(length);
            for (final BlockChange blockChange : blockChanges) {
                writer.writeVarLong((long) blockChange.newBlockId << 12 | getLocalBlockPosAsShort(blockChange.positionX, blockChange.positionY, blockChange.positionZ));
            }
        } else {
            writer.writeVarInt(0);
        }
    }

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        long chunkIndexWithSection = reader.readLong();
        chunkX = ChunkUtils.getChunkXFromChunkIndexWithSection(chunkIndexWithSection);
        chunkZ = ChunkUtils.getChunkZFromChunkIndexWithSection(chunkIndexWithSection);
        section = ChunkUtils.getSectionFromChunkIndexWithSection(chunkIndexWithSection);

        suppressLightUpdates = reader.readBoolean();

        int blockChangeCount = reader.readVarInt();
        blockChanges = new BlockChange[blockChangeCount];
        for (int i = 0; i < blockChangeCount; i++) {
            BlockChange change = new BlockChange();
            long encodedChange = reader.readVarLong();
            short localPos = (short) (encodedChange & 0x0F_FF);
            change.positionX = getXFromLocalBlockPosAsShort(localPos);
            change.positionY = getYFromLocalBlockPosAsShort(localPos);
            change.positionZ = getZFromLocalBlockPosAsShort(localPos);

            change.newBlockId = (int) (encodedChange >> 12);
            blockChanges[i] = change;
        }
    }

    public static short getLocalBlockPosAsShort(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        y = y % 16;
        z = z % Chunk.CHUNK_SIZE_Z;
        return (short) (x << 8 | z << 4 | y);
    }

    public static int getXFromLocalBlockPosAsShort(short localPos) {
        return (localPos >> 8) % Chunk.CHUNK_SIZE_X;
    }

    public static int getZFromLocalBlockPosAsShort(short localPos) {
        return (localPos >> 4) % Chunk.CHUNK_SIZE_Z;
    }

    public static int getYFromLocalBlockPosAsShort(short localPos) {
        return localPos & 0xF;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MULTI_BLOCK_CHANGE;
    }

    public static class BlockChange {
        public int positionX;
        public int positionY;
        public int positionZ;
        public int newBlockId;

    }
}
