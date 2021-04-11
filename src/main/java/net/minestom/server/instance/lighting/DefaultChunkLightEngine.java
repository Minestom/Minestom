package net.minestom.server.instance.lighting;

import net.minestom.server.instance.Chunk;

public class DefaultChunkLightEngine implements ChunkLightEngine {

    // TODO: Actually implement the lighting engine
    // Generates a bunch of random lights, lol. Pretty.
    @Override
    public void lightChunk(ChunkLight chunkLight) {
        for(int x = 0; x < Chunk.CHUNK_SIZE_X; x++)
				for(int z = 0; z < Chunk.CHUNK_SIZE_Z; z++)
					for(int y = 0; y < Chunk.CHUNK_SIZE_Y; y++)
                        chunkLight.setBlockLight(x, y, z, (int)(Math.random()*15));
    }
    
}
