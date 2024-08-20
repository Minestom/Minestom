package net.minestom.server.network.packet.server.play.data;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minestom.server.network.NetworkBuffer.*;

public record ChunkData(@NotNull CompoundBinaryTag heightmaps, byte @NotNull [] data,
                        @NotNull Map<Integer, Block> blockEntities) {
    public ChunkData {
        blockEntities = blockEntities.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().registry().isBlockEntity())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static final NetworkBuffer.Type<ChunkData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ChunkData value) {
            // Heightmaps
            buffer.write(NBT_COMPOUND, value.heightmaps);
            // Data
            buffer.write(BYTE_ARRAY, value.data);
            // Block entities
            buffer.write(VAR_INT, value.blockEntities.size());
            for (var entry : value.blockEntities.entrySet()) {
                final int index = entry.getKey();
                final Block block = entry.getValue();
                final var registry = block.registry();

                final Point point = ChunkUtils.getBlockPosition(index, 0, 0);
                buffer.write(BYTE, (byte) ((point.blockX() & 15) << 4 | point.blockZ() & 15)); // xz
                buffer.write(SHORT, (short) point.blockY()); // y

                buffer.write(VAR_INT, registry.blockEntityId());
                final CompoundBinaryTag nbt = BlockUtils.extractClientNbt(block);
                assert nbt != null;
                buffer.write(NBT, nbt); // block nbt
            }
        }

        @Override
        public ChunkData read(@NotNull NetworkBuffer buffer) {
            return new ChunkData(buffer.read(NBT_COMPOUND), buffer.read(BYTE_ARRAY),
                    readBlockEntities(buffer));
        }
    };

    private static Map<Integer, Block> readBlockEntities(@NotNull NetworkBuffer reader) {
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
