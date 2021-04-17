package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Each {@link Chunk} gets assigned to a random thread.
 */
public class PerChunkThreadProvider extends ThreadProvider {

    public PerChunkThreadProvider(int threadCount) {
        super(threadCount);
    }

    @Override
    public long findThread(@NotNull Chunk chunk) {
        return ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
    }
}
