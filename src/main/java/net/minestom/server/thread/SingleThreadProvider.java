package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Uses a single thread for all chunks.
 */
public class SingleThreadProvider extends ThreadProvider {

    public SingleThreadProvider() {
        super(1);
    }

    @Override
    public int findThread(@NotNull Chunk chunk) {
        return 0;
    }

    @Override
    public @NotNull RefreshType getChunkRefreshType() {
        return RefreshType.NEVER;
    }
}
