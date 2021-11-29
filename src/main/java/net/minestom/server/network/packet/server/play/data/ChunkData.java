package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;
import java.util.Objects;

public final class ChunkData implements Writeable {
    private final NBTCompound heightmaps;
    private final byte[] data;
    private final Map<Integer, Block> blockEntities;

    public ChunkData(NBTCompound heightmaps, byte[] data, Map<Integer, Block> blockEntities) {
        this.heightmaps = heightmaps;
        this.data = data.clone();
        this.blockEntities = Map.copyOf(blockEntities);
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
                resultNbt = NBT.Compound(nbt -> {
                    final NBTCompound blockNbt = Objects.requireNonNullElseGet(block.nbt(), NBTCompound::new);
                    for (Tag<?> tag : handler.getBlockEntityTags()) {
                        final var value = tag.read(blockNbt);
                        if (value != null) {
                            // Tag is present and valid
                            tag.writeUnsafe(nbt, value);
                        }
                    }
                });
            } else {
                // Complete nbt shall be sent if the block has no handler
                // Necessary to support all vanilla blocks
                final NBTCompound blockNbt = block.nbt();
                resultNbt = blockNbt == null ? new NBTCompound() : blockNbt;
            }
            if (resultNbt != null) writer.writeNBT("", resultNbt); // block nbt
        }
    }
}
