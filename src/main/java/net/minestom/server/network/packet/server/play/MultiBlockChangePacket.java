package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public class MultiBlockChangePacket implements ServerPacket {
    public int chunkX;
    public int chunkZ;
    public int section;
    //TODO this is important prob if we add a light api
    public boolean suppressLightUpdates = true;
    public BlockEntry[] blockChanges = new BlockEntry[0];

    public MultiBlockChangePacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(((long) (chunkX & 0x3FFFFF) << 42) | (section & 0xFFFFF) | ((long) (chunkZ & 0x3FFFFF) << 20));
        writer.writeBoolean(suppressLightUpdates);
        writer.writeArray(blockChanges);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        final long chunkIndexWithSection = reader.readLong();
        this.chunkX = (int) ((chunkIndexWithSection >> 42) & 4194303L);
        this.chunkZ = (int) ((chunkIndexWithSection >> 20) & 4194303L);
        this.section = (int) (chunkIndexWithSection & 1048575L);

        this.suppressLightUpdates = reader.readBoolean();

        final int blockChangeCount = reader.readVarInt();
        this.blockChanges = new BlockEntry[blockChangeCount];
        for (int i = 0; i < blockChangeCount; i++) {
            final long encodedChange = reader.readVarLong();
            final short localPos = (short) (encodedChange & 0x0F_FF);
            final int x = (localPos >> 8) % Chunk.CHUNK_SIZE_X;
            final int y = localPos & 0xF;
            final int z = (localPos >> 4) % Chunk.CHUNK_SIZE_Z;
            final Block block = Block.fromStateId((short) (encodedChange >> 12));
            blockChanges[i] = new BlockEntry(new Vec(x, y, z), block);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MULTI_BLOCK_CHANGE;
    }

    public static final class BlockEntry implements Writeable {
        private final Vec chunkPosition;
        public final Block block;

        public BlockEntry(Vec chunkPosition, Block block) {
            this.chunkPosition = chunkPosition;
            this.block = block;
        }

        public Vec chunkPosition() {
            return chunkPosition;
        }

        public Block block() {
            return block;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarLong((long) block.stateId() << 12 |
                    ((long) chunkPosition.blockX() << 8 |
                            (long) chunkPosition.blockZ() << 4 |
                            chunkPosition.blockY()));
        }
    }
}
