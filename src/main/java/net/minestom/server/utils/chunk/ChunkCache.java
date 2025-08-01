package net.minestom.server.utils.chunk;

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
        try {
            return instance.getBlock(x, y, z, condition);
        } catch (NullPointerException e) {
            return defaultBlock;
        }
    }
}
