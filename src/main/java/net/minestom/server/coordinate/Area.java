package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An area is a spatially connected set of block positions.
 * These areas can be used for optimizations such as instance block queries, and pathfinding domains.
 */
public sealed interface Area extends Iterable<Point> permits AreaImpl.Fill, AreaImpl.SetArea, AreaImpl.Union {

    /**
     * Creates a new area from a collection of block positions. Note that these points will be block-aligned.
     * @param collection the collection of block positions
     * @return a new area
     * @throws IllegalStateException if the resulting area is not fully connected
     */
    static @NotNull Area collection(@NotNull Collection<? extends Point> collection) {
        return AreaImpl.fromCollection(collection);
    }

    /**
     * Creates a new rectangular prism area from two points.
     * @param point1 the first (min) point
     * @param point2 the second (max) point
     * @return a new area
     */
    static @NotNull Area fill(@NotNull Point point1, @NotNull Point point2) {
        return new AreaImpl.Fill(point1, point2);
    }

    /**
     * Creates a union of multiple areas.
     * @param areas the areas to union
     * @return a new area
     */
    static @NotNull Area union(@NotNull Area... areas) {
        return new AreaImpl.Union(List.of(areas));
    }

    /**
     * Creates an intersection of multiple areas.
     * @param areas the areas to intersect
     * @return a new area
     */
    static @NotNull Area intersection(@NotNull Area... areas) {
        return AreaImpl.intersection(areas);
    }

    /**
     * Starts a path pointer used to construct an area. This is useful for pathfinding purposes.
     * @return a new path pointer
     */
    static @NotNull Area.Path path() {
        return new AreaImpl.Path();
    }

    /**
     * The minimum point of this area
     * @return the minimum point
     */
    @NotNull Point min();

    /**
     * The maximum point of this area
     * @return the maximum point
     */
    @NotNull Point max();

    /**
     * Moves this area by an offset
     * @param offset the offset
     * @return a new area
     */
    default @NotNull Area move(@NotNull Point offset) {
        Set<Point> points = StreamSupport.stream(spliterator(), false)
                .map(point -> point.add(offset))
                .collect(Collectors.toUnmodifiableSet());
        return AreaImpl.fromCollection(points);
    }

    interface Path {
        @NotNull Area.Path north(double factor);

        @NotNull Area.Path south(double factor);

        @NotNull Area.Path east(double factor);

        @NotNull Area.Path west(double factor);

        @NotNull Area.Path up(double factor);

        @NotNull Area.Path down(double factor);

        @NotNull Area end();

        default @NotNull Area.Path north() {
            return north(1);
        }

        default @NotNull Area.Path south() {
            return south(1);
        }

        default @NotNull Area.Path east() {
            return east(1);
        }

        default @NotNull Area.Path west() {
            return west(1);
        }

        default @NotNull Area.Path up() {
            return up(1);
        }

        default @NotNull Area.Path down() {
            return down(1);
        }
    }

    sealed interface HasChildren permits AreaImpl.Union {
        @NotNull Collection<Area> children();
    }
}