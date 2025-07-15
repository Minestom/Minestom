package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an area that can be generated.
 * <p>
 * The size is guaranteed to be a multiple of 16 (section).
 */
public interface GenerationUnit {
    /**
     * This unit's modifier, used to place blocks and biomes within this unit.
     *
     * @return the modifier
     */
    UnitModifier modifier();

    /**
     * The size of this unit in blocks.
     * <p>
     * Guaranteed to be a multiple of 16.
     *
     * @return the size of this unit
     */
    Point size();

    /**
     * The absolute start (min x, y, z) of this unit.
     *
     * @return the absolute start
     */
    Point absoluteStart();

    /**
     * The absolute end (max x, y, z) of this unit.
     *
     * @return the absolute end
     */
    Point absoluteEnd();

    /**
     * Creates a fork of this unit, which will be applied to the instance whenever possible.
     *
     * @param start the start of the fork
     * @param end   the end of the fork
     * @return the fork
     */
    GenerationUnit fork(Point start, Point end);

    /**
     * Creates a fork of this unit depending on the blocks placed within the consumer.
     *
     * @param consumer the consumer
     */
    void fork(Consumer<Block.Setter> consumer);

    /**
     * Divides this unit into the smallest independent units.
     *
     * @return an immutable list of independent units
     */
    default List<GenerationUnit> subdivide() {
        return List.of(this);
    }
}
