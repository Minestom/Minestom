package net.minestom.server.instance;

import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for the {@link Chunk} generation, can be set using {@link Instance#setChunkGenerator(ChunkGenerator)}.
 * <p>
 * Called if the chunk {@link IChunkLoader} hasn't been able to load the chunk.
 * @deprecated Replaced by {@link net.minestom.server.instance.generator.Generator}
 */
@Deprecated
public interface ChunkGenerator {

    /**
     * Called when the blocks in the {@link Chunk} should be set using {@link ChunkBatch#setBlock(int, int, int, Block)}
     * or similar.
     * <p>
     * WARNING: all positions are chunk-based (0-15).
     *
     * @param batch  the {@link ChunkBatch} which will be flush after the generation
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ);

    /**
     * This method is unused and is only retained for compatibility.
     */
    @Deprecated(forRemoval = true)
    @Nullable List<ChunkPopulator> getPopulators();
}
