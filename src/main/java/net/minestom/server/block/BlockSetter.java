package net.minestom.server.block;

import net.minestom.server.world.World;
import net.minestom.server.world.batch.Batch;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can place blocks at position.
 * <p>
 * Notably used by {@link World}, {@link Batch}.
 */
public interface BlockSetter {
    void setBlock(int x, int y, int z, @NotNull Block block);

    default void setBlock(@NotNull BlockPosition blockPosition, @NotNull Block block) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), block);
    }
}
