package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface GenerationUnit {
    @NotNull UnitModifier modifier();

    @NotNull Point size();

    @NotNull Point absoluteStart();

    @NotNull Point absoluteEnd();

    @NotNull GenerationUnit fork(@NotNull Point start, @NotNull Point end);

    void fork(@NotNull Consumer<Block.@NotNull Setter> consumer);

    /**
     * Divides this unit into the smallest independent units.
     *
     * @return an immutable list of independent units
     */
    default @NotNull List<GenerationUnit> subdivide() {
        return List.of(this);
    }
}
