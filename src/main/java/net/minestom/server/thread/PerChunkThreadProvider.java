package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
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
        return chunk.hashCode();
    }
}
