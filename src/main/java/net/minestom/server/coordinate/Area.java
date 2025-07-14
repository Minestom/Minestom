package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a collection of aligned block coordinates in a 3D space.
 * <p>
 * If switched over, consider a fallback to the iterator as more implementations may be added in the future.
 */
@ApiStatus.Experimental
public sealed interface Area extends Iterable<BlockVec> {
    static @NotNull Area.Single single(@NotNull Point point) {
        return new AreaImpl.Single(point.asBlockVec());
    }

    static @NotNull Area.Line line(@NotNull Point start, @NotNull Point end) {
        return new AreaImpl.Line(start.asBlockVec(), end.asBlockVec());
    }

    static @NotNull Area.Cuboid cuboid(@NotNull Point min, @NotNull Point max) {
        return new AreaImpl.Cuboid(min.asBlockVec(), max.asBlockVec());
    }

    static Area.Cuboid cube(@NotNull Point center, int size) {
        return cuboid(center.sub((double) size / 2), center.add((double) size / 2));
    }

    static Area.Cuboid box(@NotNull Point center, @NotNull Point size) {
        final Point half = size.div(2);
        return cuboid(center.sub(half), center.add(half));
    }

    static @NotNull Area.Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final Vec section = Vec.SECTION.mul(sectionX, sectionY, sectionZ);
        return cuboid(section, Vec.SECTION.add(section).sub(1));
    }

    static @NotNull Area.Sphere sphere(@NotNull Point center, int radius) {
        return new AreaImpl.Sphere(center.asBlockVec(), radius);
    }

    sealed interface Single extends Area permits AreaImpl.Single {
        @NotNull BlockVec point();
    }

    sealed interface Line extends Area permits AreaImpl.Line {
        @NotNull BlockVec start();

        @NotNull BlockVec end();
    }

    sealed interface Cuboid extends Area permits AreaImpl.Cuboid {
        @NotNull BlockVec min();

        @NotNull BlockVec max();
    }

    sealed interface Sphere extends Area permits AreaImpl.Sphere {
        @NotNull BlockVec center();

        int radius();
    }
}
