package net.minestom.server.block;

import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public interface BlockGetter {
    @NotNull Block getBlock(int x, int y, int z);

    /**
     * Gets block from given position.
     *
     * @param blockPosition position to get the block from
     * @return Block at given position.
     */
    default @NotNull Block getBlock(@NotNull BlockPosition blockPosition) {
        return getBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }
}
