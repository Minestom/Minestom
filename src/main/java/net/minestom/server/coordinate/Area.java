package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;

/**
 * Represents a collection of aligned block coordinates in a 3D space.
 * <p>
 * If switched over, consider a fallback to the iterator as more implementations may be added in the future.
 */
@ApiStatus.Experimental
@NotNullByDefault
public sealed interface Area extends Iterable<BlockVec> {

    default Area offset(int x, int y, int z) {
        return switch (this) {
            case Single single -> single(single.point().add(x, y, z));
            case Line line -> line(line.start().add(x, y, z), line.end().add(x, y, z));
            case Cuboid cuboid -> cuboid(cuboid.min().add(x, y, z), cuboid.max().add(x, y, z));
            case Sphere sphere -> sphere(sphere.center().add(x, y, z), sphere.radius());
        };
    }

    default Area offset(Point offset) {
        return offset(offset.blockX(), offset.blockY(), offset.blockZ());
    }

    static Area.Single single(Point point) {
        return new AreaImpl.Single(point.asBlockVec());
    }

    static Area.Single single(int x, int y, int z) {
        return single(new BlockVec(x, y, z));
    }

    static Area.Line line(Point start, Point end) {
        return new AreaImpl.Line(start.asBlockVec(), end.asBlockVec());
    }

    static Area.Cuboid cuboid(Point min, Point max) {
        return new AreaImpl.Cuboid(min.asBlockVec(), max.asBlockVec());
    }

    static Area.Cuboid cube(Point center, int size) {
        return cuboid(center.sub((double) size / 2), center.add((double) size / 2));
    }

    static Area.Cuboid box(Point center, Point size) {
        final Point half = size.div(2);
        return cuboid(center.sub(half), center.add(half));
    }

    static Area.Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final Vec section = Vec.SECTION.mul(sectionX, sectionY, sectionZ);
        return cuboid(section, Vec.SECTION.add(section).sub(1));
    }

    static Area.Sphere sphere(Point center, int radius) {
        return new AreaImpl.Sphere(center.asBlockVec(), radius);
    }

    sealed interface Single extends Area permits AreaImpl.Single {
        BlockVec point();
    }

    sealed interface Line extends Area permits AreaImpl.Line {
        BlockVec start();

        BlockVec end();
    }

    sealed interface Cuboid extends Area permits AreaImpl.Cuboid {
        BlockVec min();

        BlockVec max();
    }

    sealed interface Sphere extends Area permits AreaImpl.Sphere {
        BlockVec center();

        int radius();
    }
}
