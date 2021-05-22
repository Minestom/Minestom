package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.batch.Batch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can place blocks at position.
 * <p>
 * Notably used by {@link Instance}, {@link Batch}.
 */
public interface BlockModifier {

    BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    void setBlock(int x, int y, int z, @NotNull Block block);

    default void setBlock(@NotNull BlockPosition blockPosition, @NotNull Block block) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), block);
    }
}
