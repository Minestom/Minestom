package net.minestom.server.instance.batch;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public sealed interface BatchQuery
        permits BatchQueryImpl {

    static @NotNull Result fallback(Block.@NotNull Getter getter, int x, int y, int z,
                                    Block.Getter.@NotNull Condition condition, @NotNull BatchQuery query) {
        return BatchQueryImpl.fallback(getter, x, y, z, condition, query);
    }

    static @NotNull Builder builder(int radius) {
        return new BatchQueryImpl.Builder(radius);
    }

    static @NotNull BatchQuery radius(int radius) {
        return builder(radius).build();
    }

    interface Result extends Block.Getter {
        /**
         * Gets the number of blocks successfully queried.
         *
         * @return block count
         */
        int count();
    }

    sealed interface Builder
            permits BatchQueryImpl.Builder {
        @Contract("_ -> this")
        @NotNull Builder type(@NotNull Block @NotNull ... blocks);

        @Contract("_ -> this")
        @NotNull Builder exact(@NotNull Block @NotNull ... blocks);

        @NotNull BatchQuery build();
    }
}
