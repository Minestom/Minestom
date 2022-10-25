package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SectionBatch implements Batch {

    private final Short2ObjectMap<Block> blocks = new Short2ObjectOpenHashMap<>();

    protected SectionBatch() {
    }

    /**
     * Creates a new blank section batch.
     * @return a new blank section batch
     */
    public static SectionBatch create() {
        return new SectionBatch();
    }

    /**
     * Exposes a modifier to modify the blocks of this batch.
     * @param modifierConsumer the exposed modifier
     * @param chunkX the chunk x
     * @param sectionY the section y
     * @param chunkZ the chunk z
     */
    public void modify(Consumer<Modifier> modifierConsumer, int chunkX, int sectionY, int chunkZ) {
        final Modifier modifier = new Modifier(new Vec(chunkX, sectionY, chunkZ));
        synchronized (blocks) {
            modifierConsumer.accept(modifier);
        }
    }

    @Override
    public void clear() {
        synchronized (blocks) {
            blocks.clear();
        }
    }

    @Override
    public CompletableFuture<Void> apply(@NotNull Instance instance) {
        return apply(instance, 0, 0, 0);
    }

    public CompletableFuture<Void> apply(@NotNull Instance instance, int chunkX, int sectionY, int chunkZ) {
        return instance.applyBatch(this, chunkX, sectionY, chunkZ);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        synchronized (blocks) {
            short index = ChunkUtils.getSectionBlockIndex(x, y, z);
            blocks.put(index, block);
        }
    }

    public void blocks(BlockConsumer consumer) {
        synchronized (blocks) {
            for (short key : blocks.keySet()) {
                int x = ChunkUtils.getSectionBlockIndexX(key);
                int y = ChunkUtils.getSectionBlockIndexY(key);
                int z = ChunkUtils.getSectionBlockIndexZ(key);
                consumer.accept(x, y, z, blocks.get(key));
            }
        }
    }

    public interface BlockConsumer {
        void accept(int x, int y, int z, Block block);
    }

    private class Modifier implements UnitModifier {

        private final int originX;
        private final int originY;
        private final int originZ;

        public Modifier(Point start) {
            this.originX = start.blockX();
            this.originY = start.blockY();
            this.originZ = start.blockZ();
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            x = x - originX;
            y = y - originY;
            z = z - originZ;
            short index = ChunkUtils.getSectionBlockIndex(x, y, z);
            blocks.put(index, block);
        }

        @Override
        public void setRelative(int x, int y, int z, @NotNull Block block) {
            short index = ChunkUtils.getSectionBlockIndex(x, y, z);
            blocks.put(index, block);
        }

        @Override
        public void setAll(@NotNull Supplier supplier) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        int absX = x + originX;
                        int absY = y + originY;
                        int absZ = z + originZ;
                        short index = ChunkUtils.getSectionBlockIndex(x, y, z);
                        blocks.put(index, supplier.get(absX, absY, absZ));
                    }
                }
            }
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        short index = ChunkUtils.getSectionBlockIndex(x, y, z);
                        blocks.put(index, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
            for (int x = start.blockX(); x < end.blockX(); x++) {
                for (int y = start.blockY(); y < end.blockY(); y++) {
                    for (int z = start.blockZ(); z < end.blockZ(); z++) {
                        int relX = x - originX;
                        int relY = y - originY;
                        int relZ = z - originZ;
                        short index = ChunkUtils.getSectionBlockIndex(relX, relY, relZ);
                        blocks.put(index, block);
                    }
                }
            }
        }

        @Override
        public void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int y = minHeight; y < maxHeight; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        int relY = y - originY;
                        short index = ChunkUtils.getSectionBlockIndex(x, relY, z);
                        blocks.put(index, block);
                    }
                }
            }
        }

        @Override
        public void fillBiome(@NotNull Biome biome) {
            // Ignore the biome
        }

        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            // Ignore the biome
        }
    }

}
