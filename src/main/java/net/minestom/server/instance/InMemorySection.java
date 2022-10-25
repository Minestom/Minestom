package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import it.unimi.dsi.fastutil.shorts.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A mutable, non-thread-safe, in-memory section.
 */
class InMemorySection implements Section {

    private final ByteList skyLight = new ByteArrayList();
    private final ByteList blockLight = new ByteArrayList();
    private final ByteList immutableSkyLight = ByteLists.unmodifiable(skyLight);
    private final ByteList immutableBlockLight = ByteLists.unmodifiable(blockLight);

    private final Short2ObjectMap<Block> blocks = new Short2ObjectOpenHashMap<>();
    // TODO: Convert biome map to byte
    private final Short2ObjectMap<Biome> biomes = new Short2ObjectOpenHashMap<>();

    private final Short2ObjectMap<Biome> immutableBiomes = Short2ObjectMaps.unmodifiable(biomes);
    private final Short2ObjectMap<Block> immutableBlocks = Short2ObjectMaps.unmodifiable(blocks);

    public InMemorySection() {
    }

    public InMemorySection(Section other) {
        this.skyLight.setElements(other.getSkyLight().toByteArray());
        this.blockLight.setElements(other.getBlockLight().toByteArray());

        for (int x = 0; x < Section.SIZE_X; x++) {
            for (int y = 0; y < Section.SIZE_Y; y++) {
                for (int z = 0; z < Section.SIZE_Z; z++) {
                    // Block
                    this.setBlock(x, y, z, other.getBlock(x, y, z));

                    // Biome
                    if (x % 4 == 0 && y % 4 == 0 && z % 4 == 0) {
                        this.setBiome(x, y, z, other.getBiome(x, y, z));
                    }
                }
            }
        }
    }

    @Override
    public ByteList getSkyLight() {
        return immutableSkyLight;
    }

    @Override
    public void setSkyLight(ByteList skyLight) {
        this.skyLight.setElements(skyLight.toByteArray());
    }

    @Override
    public ByteList getBlockLight() {
        return immutableBlockLight;
    }

    @Override
    public void setBlockLight(ByteList blockLight) {
        this.blockLight.setElements(blockLight.toByteArray());
    }

    @Override
    public void clear() {
        this.skyLight.clear();
        this.blockLight.clear();
        this.blocks.clear();
        this.biomes.clear();
    }

    @Override
    public boolean isBlockSet(int x, int y, int z) {
        return this.blocks.containsKey(ChunkUtils.getSectionBlockIndex(x, y, z));
    }

    @Override
    public boolean isBiomeSet(int x, int y, int z) {
        return this.biomes.containsKey(ChunkUtils.getSectionBiomeIndex(x, y, z));
    }

    @Override
    public void forEachBlock(BlockConsumer consumer) {
        for (var entry : this.blocks.short2ObjectEntrySet()) {
            short index = entry.getShortKey();
            Block block = entry.getValue();

            int x = ChunkUtils.getSectionBlockIndexX(index);
            int y = ChunkUtils.getSectionBlockIndexY(index);
            int z = ChunkUtils.getSectionBlockIndexZ(index);

            consumer.accept(x, y, z, block);
        }
    }

    @Override
    public void forEachBiome(BiomeConsumer consumer) {
        for (var entry : this.biomes.short2ObjectEntrySet()) {
            short index = entry.getShortKey();
            Biome biome = entry.getValue();

            int x = ChunkUtils.getSectionBiomeIndexX(index);
            int y = ChunkUtils.getSectionBiomeIndexY(index);
            int z = ChunkUtils.getSectionBiomeIndexZ(index);

            consumer.accept(x, y, z, biome);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        this.biomes.put(ChunkUtils.getSectionBiomeIndex(x, y, z), biome);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.blocks.put(ChunkUtils.getSectionBlockIndex(x, y, z), block);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return this.blocks.get(ChunkUtils.getSectionBlockIndex(x, y, z));
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        return this.biomes.get(ChunkUtils.getSectionBiomeIndex(x, y, z));
    }
}
