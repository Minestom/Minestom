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

import static net.minestom.server.network.NetworkBuffer.*;

public record ChunkData(Map<Heightmap.Type, long[]> heightmaps, byte[] data,
                        List<BlockEntityInfo> blockEntities) {
    private static final NetworkBuffer.Type<Map<Heightmap.Type, long[]>> HEIGHTMAPS = Heightmap.Type.NETWORK_TYPE
            .mapValue(LONG_ARRAY, Heightmap.Type.values().length);
    public static final NetworkBuffer.Type<ChunkData> NETWORK_TYPE = NetworkBufferTemplate.template(
            HEIGHTMAPS, ChunkData::heightmaps,
            BYTE_ARRAY, ChunkData::data,
            BlockEntityInfo.NETWORK_TYPE.list(), ChunkData::blockEntities,
            ChunkData::new
    );

    public ChunkData {
        heightmaps = Map.copyOf(heightmaps); // TODO deep copy?
        data = data.clone();
        blockEntities = List.copyOf(blockEntities);
    }

    public record BlockEntityInfo(int index, BlockEntityType type, CompoundBinaryTag nbt) {
        public static final NetworkBuffer.Type<BlockEntityInfo> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, BlockEntityInfo value) {
                final int blockX = CoordConversion.chunkBlockIndexGetX(value.index());
                final int blockY = CoordConversion.chunkBlockIndexGetY(value.index());
                final int blockZ = CoordConversion.chunkBlockIndexGetZ(value.index());
                buffer.write(BYTE, (byte) ((blockX & 15) << 4 | blockZ & 15)); // xz
                buffer.write(SHORT, (short) blockY); // y
                buffer.write(BlockEntityType.NETWORK_TYPE, value.type());
                buffer.write(NBT, value.nbt()); // block nbt
            }

            @Override
            public BlockEntityInfo read(NetworkBuffer buffer) {
                final byte xz = buffer.read(BYTE);
                final short y = buffer.read(SHORT);
                final int index = CoordConversion.chunkBlockIndex(xz >> 4, y, xz & 15);
                final BlockEntityType blockEntityType = buffer.read(BlockEntityType.NETWORK_TYPE);
                final CompoundBinaryTag nbt = buffer.read(NBT_COMPOUND);
                return new BlockEntityInfo(index, blockEntityType, nbt);
            }
        };

        // If it's a block entity, this is safe.
        @SuppressWarnings("DataFlowIssue")
        public BlockEntityInfo(int index, Block block) {
            assert block.registry().isBlockEntity() : "Block %s is not a block entity".formatted(block.registry().key());
            this(index, block.registry().blockEntityType(), BlockUtils.extractClientNbt(block));
        }
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
}
