package net.minestom.server.utils.chunk;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Internal
public final class ChunkCache implements Block.Getter {
    private final Instance instance;
    private Chunk chunk;

    private final Block defaultBlock;

    public ChunkCache(Instance instance, Chunk chunk,
                      Block defaultBlock) {
        this.instance = instance;
        this.chunk = chunk;
        this.defaultBlock = defaultBlock;
    }

    public ChunkCache(Instance instance, Chunk chunk) {
        this(instance, chunk, Block.AIR);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        Chunk chunk = this.chunk;
        final int chunkX = CoordConversion.globalToChunk(x);
        final int chunkZ = CoordConversion.globalToChunk(z);
        if (chunk == null || !chunk.isLoaded() ||
                chunk.getChunkX() != chunkX || chunk.getChunkZ() != chunkZ) {
            this.chunk = chunk = this.instance.getChunk(chunkX, chunkZ);
        }
        if (chunk != null) {
            synchronized (chunk) {
                return chunk.getBlock(x, y, z, condition);
            }
        } else return defaultBlock;
    }
}
