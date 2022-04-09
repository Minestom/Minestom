package net.minestom.server.instance;

import net.minestom.server.instance.batch.ChunkBatch;

@Deprecated
public interface ChunkPopulator {

    void populateChunk(ChunkBatch batch, Chunk chunk);

}
