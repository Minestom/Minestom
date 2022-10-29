package net.minestom.server.instance.batch;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public sealed interface BatchQuery
        permits BatchQueryImpl {

    static @NotNull Result fallback(Block.@NotNull Getter getter, int x, int y, int z,
                                    Block.Getter.@NotNull Condition condition, @NotNull BatchQuery query) {
        return BatchQueryImpl.fallback(getter, x, y, z, condition, query);
    }

    static @NotNull BatchQuery radius(int radius) {
        return BatchQueryImpl.radius(radius);
    }

    @NotNull BatchQuery withType(@NotNull Block @NotNull ... blocks);

    @NotNull BatchQuery withExact(@NotNull Block @NotNull ... blocks);

    interface Result extends Block.Getter {
        /**
         * Gets the number of blocks successfully queried.
         *
         * @return block count
         */
        int count();
    }
}
