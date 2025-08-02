package net.minestom.server.network.packet.server.play.data;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.block.BlockUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minestom.server.network.NetworkBuffer.*;

public record ChunkData(Map<Heightmap.Type, long[]> heightmaps, byte [] data,
                        Map<Integer, Block> blockEntities) {
    public ChunkData {
        heightmaps = Map.copyOf(heightmaps);
        blockEntities = blockEntities.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().registry().isBlockEntity())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static final NetworkBuffer.Type<ChunkData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        private static final NetworkBuffer.Type<Map<Heightmap.Type, long[]>> HEIGHTMAPS = Heightmap.Type.NETWORK_TYPE
                .mapValue(LONG_ARRAY, Heightmap.Type.values().length);

        @Override
        public void write(NetworkBuffer buffer, ChunkData value) {
            // Heightmaps
            buffer.write(HEIGHTMAPS, value.heightmaps);
            // Data
            buffer.write(BYTE_ARRAY, value.data);
            // Block entities
            buffer.write(VAR_INT, value.blockEntities.size());
            for (var entry : value.blockEntities.entrySet()) {
                final int index = entry.getKey();
                final Block block = entry.getValue();
                final var registry = block.registry();

                final Point point = CoordConversion.chunkBlockIndexGetGlobal(index, 0, 0);
                buffer.write(BYTE, (byte) ((point.blockX() & 15) << 4 | point.blockZ() & 15)); // xz
                buffer.write(SHORT, (short) point.blockY()); // y

                buffer.write(VAR_INT, registry.blockEntityId());
                final CompoundBinaryTag nbt = BlockUtils.extractClientNbt(block);
                assert nbt != null;
                buffer.write(NBT, nbt); // block nbt
            }
        }

        @Override
        public ChunkData read(NetworkBuffer buffer) {
            return new ChunkData(buffer.read(HEIGHTMAPS), buffer.read(BYTE_ARRAY),
                    readBlockEntities(buffer));
        }
    };

    private static Map<Integer, Block> readBlockEntities(NetworkBuffer reader) {
        final Map<Integer, Block> blockEntities = new HashMap<>();
        final int size = reader.read(VAR_INT);
        for (int i = 0; i < size; i++) {
            final byte xz = reader.read(BYTE);
            final short y = reader.read(SHORT);
            final int blockEntityId = reader.read(VAR_INT);
            final CompoundBinaryTag nbt = reader.read(NBT_COMPOUND);
            // TODO create block object
        }
        return blockEntities;
    }
}
