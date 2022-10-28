package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.Taggable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a 16x16x16 view into a chunk.
 * Note that these views MUST be able to convert global coordinates into local coordinates.
 */
public interface Section extends Block.Getter, Block.Setter, Biome.Getter, Biome.Setter, Snapshotable {

    int SIZE_X = Chunk.SIZE_X;
    int SIZE_Y = 16;
    int SIZE_Z = Chunk.SIZE_Z;

    static @NotNull Section viewInto(Instance instance, int chunkX, int sectionY, int chunkZ) {
        return new InstanceWindowSection(instance, chunkX, sectionY, chunkZ);
    }

    static @NotNull Section inMemory() {
        return new InMemorySection();
    }

    static Section viewInto(Chunk chunk, int chunkX, int sectionY, int chunkZ) {
        return new ChunkWindowSection(chunk, chunkX, sectionY, chunkZ);
    }

    static int hash(Section section) {
        IntList hashes = new IntArrayList();
        section.forEachBlock((x, y, z, block) -> {
            hashes.add(x); hashes.add(y); hashes.add(z);
            hashes.add(block.hashCode());
        });
        return hashes.intStream().reduce(0, (a, b) -> a ^ b);
    }

    /**
     * Clears this section, removing all blocks, biomes, and light.
     */
    void clear();

    /**
     * Copies the contents of the given section into this section.
     * @param section the section to copy from
     */
    default void copy(Section section) {
        clear();
        section.forEachBlock(this::setBlock);
        section.forEachBiome(this::setBiome);
        setSkyLight(section.getSkyLight());
        setBlockLight(section.getBlockLight());
    }

    /**
     * Checks if this block has been set. Note that this is an optional optimization and may return true even if the
     * block has not been set.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the block has been set, false otherwise
     */
    default boolean isBlockSet(int x, int y, int z) {
        return true;
    }

    /**
     * Checks if this biome has been set. Note that this is an optional optimization and may return true even if the
     * biome has not been set.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the biome has been set, false otherwise
     */
    default boolean isBiomeSet(int x, int y, int z) {
        return true;
    }

    default void tick(long time) {
    }

    /**
     * Runs the block consumer for each block that has been set.
     * @param consumer the block consumer
     */
    void forEachBlock(BlockConsumer consumer);

    /**
     * Runs the biome consumer for each biome that has been set.
     * @param consumer the biome consumer
     */
    void forEachBiome(BiomeConsumer consumer);

    /**
     * @return An immutable bytelist of the skylight
     */
    ByteList getSkyLight();

    /**
     * Sets the skylight bytelist
     * @param skyLight the new skylight
     */
    void setSkyLight(ByteList skyLight);

    /**
     * Sets the skylight byte array
     * @param skyLight the new skylight
     */
    default void setSkyLight(byte[] skyLight) {
        setSkyLight(ByteList.of(skyLight));
    }

    /**
     * @return An immutable bytelist of the blocklight
     */
    ByteList getBlockLight();

    /**
     * Sets the blocklight bytelist
     * @param blockLight the new blocklight
     */
    void setBlockLight(ByteList blockLight);

    /**
     * Sets the blocklight byte array
     * @param blockLight the new blocklight
     */
    default void setBlockLight(byte[] blockLight) {
        setBlockLight(ByteList.of(blockLight));
    }

    interface BlockConsumer {
        void accept(int x, int y, int z, @NotNull Block block);
    }

    interface BiomeConsumer {
        void accept(int x, int y, int z, @NotNull Biome biome);
    }
}
