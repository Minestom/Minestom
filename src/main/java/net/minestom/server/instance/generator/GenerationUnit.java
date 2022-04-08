package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface GenerationUnit {
    /**
     * This unit's modifier, used to place blocks and biomes within this unit.
     *
     * @return the modifier
     */
    @NotNull UnitModifier modifier();

    /**
     * The size of this unit in blocks.
     *
     * @return the size of this unit
     */
    @NotNull Point size();

    /**
     * The absolute start (min x, y, z) of this unit.
     *
     * @return the absolute start
     */
    @NotNull Point absoluteStart();

    /**
     * The absolute end (max x, y, z) of this unit.
     *
     * @return the absolute end
     */
    @NotNull Point absoluteEnd();

    /**
     * Creates a fork of this unit, which will be applied to the instance whenever possible.
     *
     * @param start the start of the fork
     * @param end the end of the fork
     * @return the fork
     */
    @NotNull GenerationUnit fork(@NotNull Point start, @NotNull Point end);

    /**
     * Creates a fork of this unit depending on the blocks placed within the consumer.
     *
     * @param consumer the consumer
     */
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
