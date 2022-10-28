package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * A view into an chunk.
 */
record ChunkWindowSection(Chunk chunk, int chunkX, int sectionY, int chunkZ) implements Section {
    @Override
    public ByteList getSkyLight() {
        return chunk().getSkyLight(sectionY);
    }

    @Override
    public void setSkyLight(ByteList skyLight) {
        chunk().setSkyLight(sectionY, skyLight);
    }

    @Override
    public ByteList getBlockLight() {
        return chunk().getBlockLight(sectionY);
    }

    @Override
    public void setBlockLight(ByteList blockLight) {
        chunk().setBlockLight(sectionY, blockLight);
    }

    @Override
    public void clear() {
        IntStream.range(chunk().getMinSection(), chunk().getMaxSection())
                .mapToObj(section -> chunk().getSection(section))
                .filter(Objects::nonNull)
                .forEach(Section::clear);
    }

    @Override
    public boolean isBlockSet(int x, int y, int z) {
        return chunk.getBlock(x, y, z, Condition.NONE) != null;
    }

    @Override
    public boolean isBiomeSet(int x, int y, int z) {
        return chunk.getBiome(x, y, z) != null;
    }

    @Override
    public void forEachBlock(BlockConsumer consumer) {
        for (int x = 0; x < Section.SIZE_X; x++) {
            for (int y = 0; y < Section.SIZE_Y; y++) {
                for (int z = 0; z < Section.SIZE_Z; z++) {
                    final int absX = chunkX * Chunk.SIZE_X + x;
                    final int absY = sectionY * Section.SIZE_Y + y;
                    final int absZ = chunkZ * Chunk.SIZE_Z + z;
                    final Block block = chunk.getBlock(absX, absY, absZ);
                    consumer.accept(x, y, z, block);
                }
            }
        }
    }

    @Override
    public void forEachBiome(BiomeConsumer consumer) {
        for (int x = 0; x < Section.SIZE_X; x++) {
            for (int y = 0; y < Section.SIZE_Y; y++) {
                for (int z = 0; z < Section.SIZE_Z; z++) {
                    final int absX = chunkX * Chunk.SIZE_X + x;
                    final int absY = sectionY * Section.SIZE_Y + y;
                    final int absZ = chunkZ * Chunk.SIZE_Z + z;
                    final Biome biome = chunk.getBiome(absX, absY, absZ);
                    consumer.accept(x, y, z, biome);
                }
            }
        }
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.SIZE_X + x;
        int absY = sectionY * Section.SIZE_Y + y;
        int absZ = chunkZ * Chunk.SIZE_Z + z;
        chunk().setBiome(absX, absY, absZ, biome);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.SIZE_X + x;
        int absY = sectionY * SIZE_Y + y;
        int absZ = chunkZ * Chunk.SIZE_Z + z;
        chunk().setBlock(absX, absY, absZ, block);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.SIZE_X + x;
        int absY = sectionY * SIZE_Y + y;
        int absZ = chunkZ * Chunk.SIZE_Z + z;
        return chunk().getBlock(absX, absY, absZ, condition);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.SIZE_X + x;
        int absY = sectionY * SIZE_Y + y;
        int absZ = chunkZ * Chunk.SIZE_Z + z;
        return chunk().getBiome(absX, absY, absZ);
    }
}
