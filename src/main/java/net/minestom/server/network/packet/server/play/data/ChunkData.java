package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record ChunkData(@NotNull NBTCompound heightmaps, byte @NotNull [] data,
                        @NotNull Map<Integer, Block> blockEntities) implements Writeable {
    public ChunkData {
        blockEntities = blockEntities.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().registry().isBlockEntity())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ChunkData(BinaryReader reader) {
        this((NBTCompound) reader.readTag(), reader.readByteArray(),
                readBlockEntities(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        // Heightmaps
        writer.writeNBT("", this.heightmaps);
        // Data
        writer.writeVarInt(data.length);
        writer.writeBytes(data);
        // Block entities
        writer.writeVarInt(blockEntities.size());
        for (var entry : blockEntities.entrySet()) {
            final int index = entry.getKey();
            final Block block = entry.getValue();
            final var registry = block.registry();

            final Point point = ChunkUtils.getBlockPosition(index, 0, 0);
            writer.writeByte((byte) ((point.blockX() & 15) << 4 | point.blockZ() & 15)); // xz
            writer.writeShort((short) point.blockY()); // y

            writer.writeVarInt(registry.blockEntityId());
            final NBTCompound nbt = BlockUtils.extractClientNbt(block);
            assert nbt != null;
            writer.writeNBT("", nbt); // block nbt
        }
    }

    private static Map<Integer, Block> readBlockEntities(BinaryReader reader) {
        final Map<Integer, Block> blockEntities = new HashMap<>();
        final int size = reader.readVarInt();
        for (int i = 0; i < size; i++) {
            final byte xz = reader.readByte();
            final short y = reader.readShort();
            final int blockEntityId = reader.readVarInt();
            final NBTCompound nbt = (NBTCompound) reader.readTag();
            // TODO create block object
        }
        return blockEntities;
    }
}
