package net.minestom.server.world;

import net.minestom.server.world.batch.ChunkBatch;

public interface ChunkPopulator {

    void populateChunk(ChunkBatch batch, Chunk chunk);

}
