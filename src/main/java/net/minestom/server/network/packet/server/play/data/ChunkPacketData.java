package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

public final class ChunkPacketData implements Writeable {
    private final NBTCompound heightmaps;
    private final Map<Integer, Section> sections;
    private final Map<Integer, Block> blockEntities;

    public ChunkPacketData(NBTCompound heightmaps, Map<Integer, Section> sections, Map<Integer, Block> blockEntities) {
        this.heightmaps = heightmaps.deepClone();
        this.sections = Map.copyOf(sections);
        this.blockEntities = Map.copyOf(blockEntities);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeNBT("", this.heightmaps);
        // Data
        ByteBuffer localBuffer = PacketUtils.localBuffer();
        for (int i = 0; i < 0; i++) { // FIXME: palettes
            final Section section = Objects.requireNonNullElseGet(sections.get(i), Section::new);
            final Palette blockPalette = section.getPalette();
            writer.writeShort(blockPalette.getBlockCount());
            blockPalette.write(writer); // Blocks
            new Palette(2, 2).write(writer);  // Biomes
        }
        writer.writeVarInt(localBuffer.position());
        writer.write(localBuffer);
        // Block entities
        writer.writeVarInt(0);
    }
}
