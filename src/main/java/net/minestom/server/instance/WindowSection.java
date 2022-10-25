package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A view into an instance.
 */
record WindowSection(Instance instance, int chunkX, int sectionY, int chunkZ) implements Section {
    @Override
    public ByteList getSkyLight() {
        return instance().getSkyLight(chunkX, sectionY, chunkZ);
    }

    @Override
    public void setSkyLight(ByteList skyLight) {
        instance().setSkyLight(chunkX, sectionY, chunkZ, skyLight);
    }

    @Override
    public ByteList getBlockLight() {
        return instance().getBlockLight(chunkX, sectionY, chunkZ);
    }

    @Override
    public void setBlockLight(ByteList blockLight) {
        instance().setBlockLight(chunkX, sectionY, chunkZ, blockLight);
    }

    @Override
    public void clear() {
        instance().clearSection(chunkX, sectionY, chunkZ);
    }

    @Override
    public boolean isBlockSet(int x, int y, int z) {
        return instance.getBlock(x, y, z, Condition.NONE) != null;
    }

    @Override
    public boolean isBiomeSet(int x, int y, int z) {
        return instance.isSectionLoaded(chunkX, sectionY, chunkZ);
    }

    @Override
    public void forEachBlock(BlockConsumer consumer) {
        for (int x = 0; x < Section.SIZE_X; x++) {
            for (int y = 0; y < Section.SIZE_Y; y++) {
                for (int z = 0; z < Section.SIZE_Z; z++) {
                    final int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
                    final int absY = sectionY * Section.SIZE_Y + y;
                    final int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
                    final Block block = instance.getBlock(absX, absY, absZ);
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
                    final int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
                    final int absY = sectionY * Section.SIZE_Y + y;
                    final int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
                    final Biome biome = instance.getBiome(absX, absY, absZ);
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
        int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
        int absY = sectionY * Section.SIZE_Y + y;
        int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
        instance().setBiome(absX, absY, absZ, biome);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
        int absY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
        int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
        instance().setBlock(absX, absY, absZ, block);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
        int absY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
        int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
        return instance().getBlock(absX, absY, absZ, condition);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        x = ChunkUtils.toSectionRelativeCoordinate(x);
        y = ChunkUtils.toSectionRelativeCoordinate(y);
        z = ChunkUtils.toSectionRelativeCoordinate(z);
        int absX = chunkX * Chunk.CHUNK_SIZE_X + x;
        int absY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
        int absZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
        return instance().getBiome(absX, absY, absZ);
    }
}
