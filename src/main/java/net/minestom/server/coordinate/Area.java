package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents a collection of aligned block coordinates in a 3D space.
 * <p>
 * If switched over, consider a fallback to the iterator as more implementations may be added in the future.
 */
@ApiStatus.Experimental
public sealed interface Area extends Iterable<BlockVec> {

    /**
     * Returns this area translated by the given block offset.
     *
     * @param x the X block offset
     * @param y the Y block offset
     * @param z the Z block offset
     * @return the translated area
     */
    default Area offset(int x, int y, int z) {
        return switch (this) {
            case Single single -> single(single.point().add(x, y, z));
            case Line line -> line(line.start().add(x, y, z), line.end().add(x, y, z));
            case Cuboid cuboid -> cuboid(cuboid.min().add(x, y, z), cuboid.max().add(x, y, z));
            case Sphere sphere -> sphere(sphere.center().add(x, y, z), sphere.radius());
        };
    }

    /**
     * Returns this area translated by the block coordinates of {@code offset}.
     *
     * @param offset the offset point
     * @return the translated area
     */
    default Area offset(Point offset) {
        return offset(offset.blockX(), offset.blockY(), offset.blockZ());
    }

    /**
     * Returns the bounding box of this area.
     *
     * @return a cuboid representing the bounding box with the lowest and highest points
     */
    default Cuboid bound() {
        return switch (this) {
            case Single single -> cuboid(single.point(), single.point());
            case Line line -> {
                final BlockVec start = line.start();
                final BlockVec end = line.end();
                yield cuboid(start.min(end), start.max(end));
            }
            case Cuboid cuboid -> cuboid;
            case Sphere sphere -> {
                final BlockVec center = sphere.center();
                final int radius = sphere.radius();
                yield cuboid(center.sub(radius, radius, radius), center.add(radius, radius, radius));
            }
        };
    }

    /**
     * Checks whether this area contains the block coordinate of {@code point}.
     *
     * @param point the point to convert to a block coordinate
     * @return {@code true} if the block coordinate is contained in this area
     */
    boolean contains(Point point);

    /**
     * Returns the number of blocks contained in this area.
     * <p>
     * Counting a sphere requires scanning its bounding box and may be expensive for large radii.
     *
     * @return the contained block count
     */
    long blockCount();

    /**
     * Splits this area into multiple cuboids which do not cross section boundaries.
     * <p>
     * Single sections may have multiple cuboids if the section-local portion is not a perfect cuboid.
     *
     * @return list of sub-cuboids covering exactly this area
     */
    List<Cuboid> split();

    /**
     * Creates an area containing a single block.
     *
     * @param point the point to convert to a block coordinate
     * @return a single-block area
     */
    static Single single(Point point) {
        return new AreaImpl.Single(point.asBlockVec());
    }

    /**
     * Creates an area containing a single block.
     *
     * @param x the block X coordinate
     * @param y the block Y coordinate
     * @param z the block Z coordinate
     * @return a single-block area
     */
    static Single single(int x, int y, int z) {
        return single(new BlockVec(x, y, z));
    }

    /**
     * Creates a line area between two block coordinates.
     *
     * @param start the start point to convert to a block coordinate
     * @param end   the end point to convert to a block coordinate
     * @return a line area
     */
    static Line line(Point start, Point end) {
        return new AreaImpl.Line(start.asBlockVec(), end.asBlockVec());
    }

    /**
     * Creates a cuboid area from two corners. The corners may be supplied in any order.
     *
     * @param min one corner to convert to a block coordinate
     * @param max the other corner to convert to a block coordinate
     * @return a cuboid area with ordered minimum and maximum coordinates
     */
    static Cuboid cuboid(Point min, Point max) {
        return new AreaImpl.Cuboid(min.asBlockVec(), max.asBlockVec());
    }

    /**
     * Creates a cuboid centered around {@code center} with the same size on each axis.
     * <p>
     * Since the bounds are inclusive block coordinates, even sizes include the center block and
     * extend {@code size / 2} blocks in each direction. The size is a coordinate span, not the
     * final number of blocks.
     *
     * @param center the center point to convert to a block coordinate
     * @param size   the size used for each axis
     * @return a cuboid area
     */
    static Cuboid cube(Point center, int size) {
        return cuboid(center.sub((double) size / 2), center.add((double) size / 2));
    }

    /**
     * Creates a cuboid centered around {@code center} with the given size on each axis.
     * <p>
     * Since the bounds are inclusive block coordinates, even sizes include the center block and
     * extend half of the size in each direction. The size is a coordinate span, not the final
     * number of blocks.
     *
     * @param center the center point to convert to a block coordinate
     * @param size   the size point, converted through its coordinates
     * @return a cuboid area
     */
    static Cuboid box(Point center, Point size) {
        final Point half = size.div(2);
        return cuboid(center.sub(half), center.add(half));
    }

    /**
     * Creates a cuboid containing all blocks in the given section.
     *
     * @param sectionX the section X coordinate
     * @param sectionY the section Y coordinate
     * @param sectionZ the section Z coordinate
     * @return a 16x16x16 section cuboid
     */
    static Cuboid section(int sectionX, int sectionY, int sectionZ) {
        final BlockVec section = BlockVec.SECTION.mul(sectionX, sectionY, sectionZ);
        return cuboid(section, BlockVec.SECTION.add(section).sub(1));
    }

    /**
     * Creates a sphere area from a center and non-negative radius.
     *
     * @param center the center point to convert to a block coordinate
     * @param radius the radius in blocks
     * @return a sphere area
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    static Sphere sphere(Point center, int radius) {
        return new AreaImpl.Sphere(center.asBlockVec(), radius);
    }

    /**
     * An area containing exactly one block.
     */
    sealed interface Single extends Area permits AreaImpl.Single {
        /**
         * @return the contained block
         */
        BlockVec point();
    }

    /**
     * An area containing blocks traced by a line between two block coordinates.
     */
    sealed interface Line extends Area permits AreaImpl.Line {
        /**
         * @return the start block
         */
        BlockVec start();

        /**
         * @return the end block
         */
        BlockVec end();
    }

    /**
     * An area containing all blocks inside an inclusive cuboid.
     */
    sealed interface Cuboid extends Area permits AreaImpl.Cuboid {
        /**
         * @return the minimum corner
         */
        BlockVec min();

        /**
         * @return the maximum corner
         */
        BlockVec max();
    }

    /**
     * An area containing all blocks within a radius of a center block.
     */
    sealed interface Sphere extends Area permits AreaImpl.Sphere {
        /**
         * @return the center block
         */
        BlockVec center();

        /**
         * @return the non-negative radius
         */
        int radius();
    }
}
