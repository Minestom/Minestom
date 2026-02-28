package net.minestom.server.utils.chunk;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Internal
public final class ChunkCache implements Block.Getter {
    private final Instance instance;
    private @Nullable Chunk chunk;

    private final @Nullable Block defaultBlock;

    public ChunkCache(Instance instance, @Nullable Chunk chunk,
                      @Nullable Block defaultBlock) {
        this.instance = instance;
        this.chunk = chunk;
        this.defaultBlock = defaultBlock;
    }

    public ChunkCache(Instance instance, Chunk chunk) {
        this(instance, chunk, Block.AIR);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, Condition condition) {
        Chunk chunk = this.chunk;
        final int chunkX = CoordConversion.globalToChunk(x);
        final int chunkZ = CoordConversion.globalToChunk(z);
        if (chunk == null || !chunk.isLoaded() ||
                chunk.getChunkX() != chunkX || chunk.getChunkZ() != chunkZ) {
            this.chunk = chunk = this.instance.getChunk(chunkX, chunkZ);
        }
        if (chunk != null) {
            chunk.lockReadLock();
            try {
                return chunk.getBlock(x, y, z, condition);
            } finally {
                chunk.unlockReadLock();
            }
        } else return defaultBlock;
    }
}
