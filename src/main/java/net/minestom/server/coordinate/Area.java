package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

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

    /**
     * Returns the bounding box of this
     *
     * @return a cuboid representing the bounding box with the lowest and highest points
     */
    default Cuboid bound() {
        return switch (this) {
            case Single single -> cuboid(single.point(), single.point());
            case Line line -> {
                BlockVec start = line.start();
                BlockVec end = line.end();
                yield cuboid(
                        new BlockVec(Math.min(start.blockX(), end.blockX()),
                                Math.min(start.blockY(), end.blockY()),
                                Math.min(start.blockZ(), end.blockZ())),
                        new BlockVec(Math.max(start.blockX(), end.blockX()),
                                Math.max(start.blockY(), end.blockY()),
                                Math.max(start.blockZ(), end.blockZ()))
                );
            }
            case Cuboid cuboid -> cuboid;
            case Sphere sphere -> {
                BlockVec center = sphere.center();
                int radius = sphere.radius();
                yield cuboid(center.sub(radius, radius, radius), center.add(radius, radius, radius));
            }
        };
    }

    /**
     * Splits this area into multiple section aligned cuboids.
     * <p>
     * Single sections may have multiple cuboids if they are not perfect cuboids.
     *
     * @return list of sub-cuboids covering this area
     */
    List<Cuboid> split();

    static Single single(Point point) {
        return new AreaImpl.Single(point.asBlockVec());
    }

    static Single single(int x, int y, int z) {
        return single(new BlockVec(x, y, z));
    }

    static Line line(Point start, Point end) {
        return new AreaImpl.Line(start.asBlockVec(), end.asBlockVec());
    }

    static Cuboid cuboid(Point min, Point max) {
        return new AreaImpl.Cuboid(min.asBlockVec(), max.asBlockVec());
    }

    static Cuboid cube(Point center, int size) {
        return cuboid(center.sub((double) size / 2), center.add((double) size / 2));
    }

    static Cuboid box(Point center, Point size) {
        final Point half = size.div(2);
        return cuboid(center.sub(half), center.add(half));
    }

    static Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final BlockVec section = BlockVec.SECTION.mul(sectionX, sectionY, sectionZ);
        return cuboid(section, BlockVec.SECTION.add(section).sub(1));
    }

    static Sphere sphere(Point center, int radius) {
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
