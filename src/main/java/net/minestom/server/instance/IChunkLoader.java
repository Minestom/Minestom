package net.minestom.server.instance;

import org.jetbrains.annotations.Nullable;

/**
 * Deprecated for removal use {@link ChunkLoader} instead.
 */
@Deprecated(forRemoval = true)
public interface IChunkLoader extends ChunkLoader {

    /**
     * Use {@link ChunkLoader#noop()} instead.
     * @return the noop
     */
    @Deprecated
    static IChunkLoader noop() {
        return new IChunkLoader() { // Inlined cause removal.
            @Override
            public @Nullable Chunk loadChunk(Instance instance, int chunkX, int chunkZ) {
                return null;
            }

            @Override
            public void saveChunk(Chunk chunk) {
                // empty
            }
        };
    }
}
