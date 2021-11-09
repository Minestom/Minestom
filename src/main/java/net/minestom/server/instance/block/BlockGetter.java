package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public interface BlockGetter {
    @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition);

    default @UnknownNullability Block getBlock(@NotNull Point point, @NotNull Condition condition) {
        return getBlock(point.blockX(), point.blockY(), point.blockZ(), condition);
    }

    default @NotNull Block getBlock(int x, int y, int z) {
        return Objects.requireNonNull(getBlock(x, y, z, Condition.NONE));
    }

    default @NotNull Block getBlock(@NotNull Point point) {
        return Objects.requireNonNull(getBlock(point, Condition.NONE));
    }

    /**
     * Represents a hint to retrieve blocks more efficiently.
     * Implementing interfaces do not have to honor this.
     */
    @ApiStatus.Experimental
    enum Condition {
        /**
         * Returns a block no matter what.
         * {@link Block#AIR} being the default result.
         */
        NONE,
        /**
         * Hints that the method should return only if the block is cached.
         * <p>
         * Useful if you are only interested in a block handler or nbt.
         */
        CACHED,
        /**
         * Hints that we only care about the block type.
         * <p>
         * Useful if you need to retrieve registry information about the block.
         * Be aware that the returned block may not return the proper handler/nbt.
         */
        TYPE
    }
}
