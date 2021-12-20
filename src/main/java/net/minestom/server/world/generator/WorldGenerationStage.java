package net.minestom.server.world.generator;

import net.minestom.server.instance.batch.ChunkBatch;

public interface WorldGenerationStage {
    /**
     * Called for generating current chunk data, if it isn't overridden it calls {@link #lookAround(GenerationContext, int, int)}
     * as a shorthand for generating non block data for chunks<br>
     * WARNING: DO NOT CALL SUPER!
     * @param context the generator, used to get results of previous stages and save results of this stage
     * @param batch used to set blocks in the final chunk
     * @param chunkX chunk's X coordinate
     * @param chunkZ chunk's Z coordinate
     */
    default void process(GenerationContext context, ChunkBatch batch, int chunkX, int chunkZ) {
        lookAround(context, chunkX, chunkZ);
    }

    /**
     * Called for chunks in the {@link #getLookAroundRange() look around} range<br>
     * WARNING: DO NOT CALL SUPER!
     * @param context the generator, used to get results of previous stages and save results of this stage
     * @param chunkX chunk's X coordinate
     * @param chunkZ chunk's Z coordinate
     */
    default void lookAround(GenerationContext context, int chunkX, int chunkZ) {
        if (getLookAroundRange() > 0) {
            throw new IllegalStateException("Look around is enabled, but method isn't overridden!");
        } else {
            throw new IllegalStateException("Look around is disabled in this stage!");
        }
    }

    /**
     * The range in what the generator should look around, e.g. for generating structures<br>
     * Use 0 to disable lookaround
     * @return range >= 0
     */
    int getLookAroundRange();
}
