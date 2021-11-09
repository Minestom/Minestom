package net.minestom.server.network.packet.server.play;

import it.unimi.dsi.fastutil.ints.Int2LongRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class ChunkDataPacket implements ServerPacket {

    public Biome[] biomes;
    public int chunkX, chunkZ;

    public Map<Integer, Section> sections = new HashMap<>();
    public Map<Integer, Block> entries = new HashMap<>();

    private static final byte CHUNK_SECTION_COUNT = 16;

    public NBTCompound heightmapsNBT = new NBTCompound();

    public ChunkDataPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);

        ByteBuffer blocks = PacketUtils.localBuffer();

        Int2LongRBTreeMap maskMap = new Int2LongRBTreeMap();

        for (var entry : sections.entrySet()) {
            final int index = entry.getKey();
            final Section section = entry.getValue();

            final int lengthIndex = index % 64;
            final int maskIndex = index / 64;

            long mask = maskMap.get(maskIndex);
            mask |= 1L << lengthIndex;
            maskMap.put(maskIndex, mask);

            Utils.writePaletteBlocks(blocks, section.getPalette());
        }

        final int maskSize = maskMap.size();
        writer.writeVarInt(maskSize);
        for (int i = 0; i < maskSize; i++) {
            final long value = maskMap.getOrDefault(i, 0);
            writer.writeLong(value);
        }

        // Heightmap
        writer.writeNBT("", heightmapsNBT);

        // Biomes
        if (biomes == null || biomes.length == 0) {
            writer.writeVarInt(0);
        } else {
            writer.writeVarInt(biomes.length);
            for (Biome biome : biomes) {
                writer.writeVarInt(biome.getId());
            }
        }

        // Data
        writer.writeVarInt(blocks.position());
        writer.write(blocks);

        // Block entities
        if (entries == null || entries.isEmpty()) {
            writer.writeVarInt(0);
        } else {
            List<NBTCompound> compounds = new ArrayList<>();
            for (var entry : entries.entrySet()) {
                final int index = entry.getKey();
                final Block block = entry.getValue();
                final String blockEntity = block.registry().blockEntity();
                if (blockEntity == null) continue; // Only send block entities to client
                NBTCompound resultNbt = new NBTCompound();
                // Append handler tags
                final BlockHandler handler = block.handler();
                if (handler != null) {
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
                    if (blockNbt != null) resultNbt = blockNbt;
                }
                // Add block entity
                final var blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);
                resultNbt.setString("id", blockEntity)
                        .setInt("x", blockPosition.blockX())
                        .setInt("y", blockPosition.blockY())
                        .setInt("z", blockPosition.blockZ());
                compounds.add(resultNbt);
            }
            writer.writeVarInt(compounds.size());
            compounds.forEach(nbtCompound -> writer.writeNBT("", nbtCompound));
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.chunkX = reader.readInt();
        this.chunkZ = reader.readInt();

        int maskCount = reader.readVarInt();
        long[] masks = new long[maskCount];
        for (int i = 0; i < maskCount; i++) {
            masks[i] = reader.readLong();
        }
        try {
            // TODO: Use heightmaps
            // unused at the moment
            this.heightmapsNBT = (NBTCompound) reader.readTag();

            // Biomes
            int[] biomesIds = reader.readVarIntArray();
            this.biomes = new Biome[biomesIds.length];
            for (int i = 0; i < biomesIds.length; i++) {
                this.biomes[i] = MinecraftServer.getBiomeManager().getById(biomesIds[i]);
            }

            // Data
            this.sections = new HashMap<>();
            int blockArrayLength = reader.readVarInt();
            if (maskCount > 0) {
                final long mask = masks[0]; // TODO support for variable size
                for (int sectionIndex = 0; sectionIndex < CHUNK_SECTION_COUNT; sectionIndex++) {
                    final boolean hasSection = (mask & 1 << sectionIndex) != 0;
                    if (!hasSection) continue;
                    final Section section = sections.computeIfAbsent(sectionIndex, i -> new Section());
                    final Palette palette = section.getPalette();
                    final short blockCount = reader.readShort();
                    palette.setBlockCount(blockCount);
                    final byte bitsPerEntry = reader.readByte();
                    // Resize palette if necessary
                    if (bitsPerEntry > palette.getBitsPerEntry()) {
                        palette.resize(bitsPerEntry);
                    }
                    // Retrieve palette values
                    if (bitsPerEntry < 9) {
                        int paletteSize = reader.readVarInt();
                        for (int i = 0; i < paletteSize; i++) {
                            final int paletteValue = reader.readVarInt();
                            palette.getPaletteBlockArray()[i] = (short) paletteValue;
                            palette.getBlockPaletteMap().put((short) paletteValue, (short) i);
                        }
                    }
                    // Read blocks
                    palette.setBlocks(reader.readLongArray());
                }
            }

            // Block entities
            final int blockEntityCount = reader.readVarInt();
            this.entries = new Int2ObjectOpenHashMap<>(blockEntityCount);
            for (int i = 0; i < blockEntityCount; i++) {
                NBTCompound tag = (NBTCompound) reader.readTag();
                final String id = tag.getString("id");
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandlerOrDummy(id);
                final int x = tag.getInt("x");
                final int y = tag.getInt("y");
                final int z = tag.getInt("z");
                // TODO add to handlerMap & nbtMap
            }
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            // TODO: should we throw to avoid an invalid packet?
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }
}