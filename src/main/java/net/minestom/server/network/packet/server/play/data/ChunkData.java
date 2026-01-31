package net.minestom.server.network.packet.server.play.data;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.block.BlockUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ChunkData(Map<Heightmap.Type, long[]> heightmaps, byte[] data,
                        List<BlockEntityInfo> blockEntities) {
    public static final NetworkBuffer.Type<ChunkData> NETWORK_TYPE = NetworkBufferTemplate.template(
            Heightmap.Type.NETWORK_TYPE.mapValue(LONG_ARRAY, Heightmap.Type.values().length), ChunkData::heightmaps,
            BYTE_ARRAY, ChunkData::data,
            BlockEntityInfo.NETWORK_TYPE.list(), ChunkData::blockEntities,
            ChunkData::new
    );

    public ChunkData {
        heightmaps = Map.copyOf(heightmaps); // TODO deep copy?
        data = data.clone();
        blockEntities = List.copyOf(blockEntities);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChunkData(
                Map<Heightmap.Type, long[]> heightmaps1, byte[] data1, List<BlockEntityInfo> entities
        ))) return false;
        return Arrays.equals(data, data1) && blockEntities.equals(entities) && heightmaps.equals(heightmaps1);
    }

    @Override
    public int hashCode() {
        int result = heightmaps.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + blockEntities.hashCode();
        return result;
    }

    public record BlockEntityInfo(int index, BlockEntityType type, CompoundBinaryTag nbt) {
        public static final NetworkBuffer.Type<BlockEntityInfo> NETWORK_TYPE = NetworkBufferTemplate.template(
                BYTE, BlockEntityInfo::xz,
                SHORT, BlockEntityInfo::y,
                BlockEntityType.NETWORK_TYPE, BlockEntityInfo::type,
                NBT_COMPOUND, BlockEntityInfo::nbt,
                BlockEntityInfo::new
        );

        public BlockEntityInfo {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(nbt, "nbt");
        }

        // If it's a block entity, this is safe.
        @SuppressWarnings("DataFlowIssue")
        public BlockEntityInfo(int index, Block block) {
            assert block.registry().isBlockEntity() : "Block %s is not a block entity".formatted(block.registry().key());
            this(index, block.registry().blockEntityType(), BlockUtils.extractClientNbt(block));
        }

        // Serialization below this point
        private BlockEntityInfo(byte xz, short blockY, BlockEntityType type, CompoundBinaryTag tag) {
            this(CoordConversion.chunkBlockIndex(xz >> 4, blockY, xz & 15), type, tag);
        }

        private byte xz() {
            final int blockX = CoordConversion.chunkBlockIndexGetX(index);
            final int blockZ = CoordConversion.chunkBlockIndexGetZ(index);
            return (byte) ((blockX & 15) << 4 | (blockZ & 15));
        }

        private short y() {
            final int blockY = CoordConversion.chunkBlockIndexGetY(index);
            return (short) blockY;
        }
    }
}
