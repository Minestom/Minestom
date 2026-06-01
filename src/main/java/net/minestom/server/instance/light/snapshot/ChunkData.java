package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.light.LightingChunk;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ChunkData {
    final LightingChunk chunk;
    private final AtomicReference<@Nullable OcclusionData> occlusionData = new AtomicReference<>(null);

    public ChunkData(LightingChunk chunk) {
        this.chunk = chunk;
    }

    public void invalidateOcclusionData() {
        occlusionData.set(null);
    }

    public OcclusionData occlusionData() {
        var data = occlusionData.get();
        if (data != null) return data;
        chunk.lockReadLock();
        try {
            var highestBlock = chunk.getHighestBlock();
            var occlusionMap = chunk.getOcclusionMap();
            data = new OcclusionData(highestBlock, occlusionMap);
            var old = occlusionData.compareAndExchange(null, data);
            return old == null ? data : old;
        } finally {
            chunk.unlockReadLock();
        }
    }
}
