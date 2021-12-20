package net.minestom.server.world.generator.stages;

import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.WorldGenerationStage;

public class HeightMapStage implements WorldGenerationStage {
    @Override
    public void process(GenerationContext context, ChunkBatch batch, int chunkX, int chunkZ) {

    }

    @Override
    public int getLookAroundRange() {
        return 0;
    }
}
