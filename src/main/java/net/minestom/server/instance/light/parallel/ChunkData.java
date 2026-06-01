package net.minestom.server.instance.light.parallel;

import net.minestom.server.instance.light.LightEngine;
import net.minestom.server.instance.light.LightingChunk;
import net.minestom.server.instance.light.NeighborSnapshot;

public class ChunkData {
    final LightingChunk chunk;
    final LightEngine.WorkTypeTracker<Integer> trackerPalette = new LightEngine.WorkTypeTracker.Hash<>();
    final LightEngine.WorkTypeTracker<Integer> trackerBlockLightExternal = new LightEngine.WorkTypeTracker.Hash<>();
    final LightEngine.WorkTypeTracker<Integer> trackerSkyLightExternal = new LightEngine.WorkTypeTracker.Hash<>();
    final LightEngine.WorkTypeTracker<Integer> trackerFullBlockRelight = new LightEngine.WorkTypeTracker.Hash<>();
    final LightEngine.WorkTypeTracker<Integer> trackerFullSkyRelight = new LightEngine.WorkTypeTracker.Hash<>();

    public ChunkData(LightingChunk chunk) {
        this.chunk = chunk;
    }

    NeighborSnapshot createNeighborSnapshot() {
        return chunk.createNeighborSnapshot();
    }

    int getChunkX() {
        return chunk.getChunkX();
    }

    int getChunkZ() {
        return chunk.getChunkX();
    }
}
