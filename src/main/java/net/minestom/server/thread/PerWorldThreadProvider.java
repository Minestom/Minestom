package net.minestom.server.thread;

import net.minestom.server.world.Chunk;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Each {@link World} gets assigned to a random thread.
 */
public class PerWorldThreadProvider extends ThreadProvider {

    public PerWorldThreadProvider(int threadCount) {
        super(threadCount);
    }

    public PerWorldThreadProvider() {
        super();
    }

    @Override
    public long findThread(@NotNull Chunk chunk) {
        return chunk.getWorld().hashCode();
    }

    @Override
    public @NotNull RefreshType getChunkRefreshType() {
        return RefreshType.NEVER;
    }
}
