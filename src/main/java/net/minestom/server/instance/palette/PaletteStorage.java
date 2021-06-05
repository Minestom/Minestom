package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

/**
 * Used to efficiently store blocks with an optional palette.
 * <p>
 * The format used is the one described in the {@link net.minestom.server.network.packet.server.play.ChunkDataPacket},
 * the reason is that it allows us to write the packet much faster.
 */
public class PaletteStorage implements PublicCloneable<PaletteStorage> {

    private Int2ObjectRBTreeMap<Section> sectionMap = new Int2ObjectRBTreeMap<>();

    private final int defaultBitsPerEntry;
    private final int defaultBitsIncrement;

    /**
     * Creates a new palette storage.
     *
     * @param defaultBitsPerEntry  the number of bits used for one entry (block)
     * @param defaultBitsIncrement the number of bits to add per-block once the palette array is filled
     */
    public PaletteStorage(int defaultBitsPerEntry, int defaultBitsIncrement) {
        Check.argCondition(defaultBitsPerEntry > Section.MAXIMUM_BITS_PER_ENTRY,
                "The maximum bits per entry is 15");
        this.defaultBitsPerEntry = defaultBitsPerEntry;
        this.defaultBitsIncrement = defaultBitsIncrement;
    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        final int sectionIndex = ChunkUtils.getSectionAt(y);
        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        Section section = getSection(sectionIndex);
        if (section == null) {
            section = new Section(defaultBitsPerEntry, defaultBitsIncrement);
            setSection(sectionIndex, section);
        }
        section.setBlockAt(x, y, z, blockId);
    }

    public short getBlockAt(int x, int y, int z) {
        final int sectionIndex = ChunkUtils.getSectionAt(y);
        final Section section = getSection(sectionIndex);
        if (section == null) {
            return Block.AIR.getBlockId();
        }
        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        return section.getBlockAt(x, y, z);
    }

    public Int2ObjectRBTreeMap<Section> getSectionMap() {
        return sectionMap;
    }

    public @Nullable Collection<Section> getSections() {
        return sectionMap.values();
    }

    public @Nullable Section getSection(int section) {
        return sectionMap.get(section);
    }

    /**
     * Loops through all the sections and blocks to find unused array (empty chunk section)
     * <p>
     * Useful after clearing one or multiple sections of a chunk. Can be unnecessarily expensive if the chunk
     * is composed of almost-empty sections since the loop will not stop until a non-air block is discovered.
     */
    public synchronized void clean() {
        getSections().forEach(Section::clean);
    }

    /**
     * Clears all the data in the palette and data array.
     */
    public void clear() {
        getSections().forEach(Section::clear);
    }

    @NotNull
    @Override
    public PaletteStorage clone() {
        try {
            PaletteStorage paletteStorage = (PaletteStorage) super.clone();
            // TODO deep clone
            paletteStorage.sectionMap = sectionMap.clone();//CloneUtils.cloneArray(sections, Section[]::new);
            return paletteStorage;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }

    private void setSection(int sectionIndex, Section section) {
        this.sectionMap.put(sectionIndex, section);
    }

    /**
     * Converts a world coordinate to a chunk one.
     *
     * @param xz the world coordinate
     * @return the chunk coordinate of {@code xz}
     */
    private static int toChunkCoordinate(int xz) {
        xz %= 16;
        if (xz < 0) {
            xz += CHUNK_SECTION_SIZE;
        }

        return xz;
    }
}
