package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public interface BlockGetter {
    @NotNull Block getBlock(int x, int y, int z);

    /**
     * Gets block from given position.
     *
     * @param point position to get the block from
     * @return Block at given position.
     */
    default @NotNull Block getBlock(@NotNull Point point) {
        return getBlock(point.blockX(), point.blockY(), point.blockZ());
    }
}
