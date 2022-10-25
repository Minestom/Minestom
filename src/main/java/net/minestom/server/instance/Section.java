package net.minestom.server.instance;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

/**
 * Represents a 16x16x16 view into an instance.
 */
public interface Section extends Block.Getter, Block.Setter, Biome.Getter, Biome.Setter {

    int SIZE_X = Chunk.CHUNK_SIZE_X;
    int SIZE_Y = Chunk.CHUNK_SECTION_SIZE;
    int SIZE_Z = Chunk.CHUNK_SIZE_Z;

    static @NotNull Section viewInto(Instance instance, int chunkX, int sectionY, int chunkZ) {
        return new WindowSection(instance, chunkX, sectionY, chunkZ);
    }

    static @NotNull Section inMemory() {
        return new InMemorySection();
    }

    ByteList getSkyLight();

    void setSkyLight(ByteList skyLight);
    default void setSkyLight(byte[] skyLight) {
        setSkyLight(ByteList.of(skyLight));
    }

    ByteList getBlockLight();

    void setBlockLight(ByteList blockLight);
    default void setBlockLight(byte[] blockLight) {
        setBlockLight(ByteList.of(blockLight));
    }

    void clear();

    boolean isBlockSet(int x, int y, int z);
    boolean isBiomeSet(int x, int y, int z);

    void forEachBlock(BlockConsumer consumer);
    void forEachBiome(BiomeConsumer consumer);

    interface BlockConsumer {
        void accept(int x, int y, int z, @NotNull Block block);
    }

    interface BiomeConsumer {
        void accept(int x, int y, int z, @NotNull Biome biome);
    }
}
