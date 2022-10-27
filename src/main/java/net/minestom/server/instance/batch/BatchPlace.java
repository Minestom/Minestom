package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.UnitModifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Experimental
public sealed interface BatchPlace
        permits BatchPlaceImpl {
    static @NotNull BatchPlace batch(@NotNull Point size,
                                     @NotNull Consumer<@NotNull UnitModifier> modifier) {
        return BatchPlaceImpl.batch(size, modifier);
    }

    void forEachBlock(int originX, int originY, int originZ,
                      @NotNull BiConsumer<@NotNull Block, @NotNull Point> consumer);
}
