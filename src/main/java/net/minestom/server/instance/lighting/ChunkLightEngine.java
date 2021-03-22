package net.minestom.server.instance.lighting;

import org.jetbrains.annotations.NotNull;

/**
Chunk Light API - Customize light for each chunk
 */
public interface ChunkLightEngine {

    public void lightChunk(@NotNull ChunkLight chunkLight);

}