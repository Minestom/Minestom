package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ChunkData(NBTCompound heightmaps, byte[] data,
                        Map<Integer, Block> blockEntities) implements Writeable {
    public ChunkData {
        heightmaps = heightmaps.deepClone();
        data = data.clone();
        blockEntities = Map.copyOf(blockEntities);
    }

    public ChunkData(BinaryReader reader) {
        this((NBTCompound) reader.readTag(),
                reader.readBytes(reader.readVarInt()),
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
            if (!registry.isBlockEntity()) continue;

            final Point point = ChunkUtils.getBlockPosition(index, 0, 0);

            writer.writeByte((byte) ((point.blockX() & 15) << 4 | point.blockZ() & 15)); // xz
            writer.writeShort((short) point.blockY()); // y

            writer.writeVarInt(registry.blockEntityId());


            NBTCompound resultNbt;
            // Append handler tags
            final BlockHandler handler = block.handler();
            if (handler != null) {
                resultNbt = new NBTCompound();
                final NBTCompound blockNbt = Objects.requireNonNullElseGet(block.nbt(), NBTCompound::new);
                for (Tag<?> tag : handler.getBlockEntityTags()) {
                    final var value = tag.read(blockNbt);
                    if (value != null) {
                        // Tag is present and valid
                        tag.writeUnsafe(resultNbt, value);
                    }
                }
            } else {
                // Complete nbt shall be sent if the block has no handler
                // Necessary to support all vanilla blocks
                final NBTCompound blockNbt = block.nbt();
                resultNbt = blockNbt == null ? new NBTCompound() : blockNbt;
            }
            writer.writeNBT("", resultNbt); // block nbt
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
