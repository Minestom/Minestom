package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface BlockGetter {
    @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition);

    default @Nullable Block getBlock(@NotNull Point point, @NotNull Condition condition) {
        return getBlock(point.blockX(), point.blockY(), point.blockZ(), condition);
    }

    default @NotNull Block getBlock(int x, int y, int z) {
        return Objects.requireNonNull(getBlock(x, y, z, Condition.NONE));
    }

    default @NotNull Block getBlock(@NotNull Point point) {
        return Objects.requireNonNull(getBlock(point, Condition.NONE));
    }

    enum Condition {
        /**
         * Returns a block no matter what.
         * {@link Block#AIR} being the default result.
         */
        NONE,
        /**
         * Returns a block only if it has a handler or nbt.
         * <p>
         * Should be more performant than {@link #NONE}.
         */
        CACHED
    }
}
