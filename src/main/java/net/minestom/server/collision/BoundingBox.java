package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox implements Collidable {
    private final double width, height, depth;
    private final BoundingBoxUtils.Faces faces;

    public boolean relativeCollision(Collidable boundingBox, Point point) {
        return true;
    }

    public BoundingBox(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.faces = BoundingBoxUtils.retrieveFaces(this);
    }

    /**
     * Used to know if two {@link BoundingBox} intersect with each other.
     *
     * @param boundingBox the {@link BoundingBox} to check
     * @return true if the two {@link BoundingBox} intersect with each other, false otherwise
     */
    public boolean intersectBoundingBox(@NotNull Point src, @NotNull BoundingBox boundingBox, @NotNull Point dest) {
        return (minX() + src.x() <= boundingBox.maxX() + dest.x() && maxX() + src.x() >= boundingBox.minX() + dest.x()) &&
                (minY() + src.y() <= boundingBox.maxY() + dest.y() && maxY() + src.y() >= boundingBox.minY() + dest.y()) &&
                (minZ() + src.z() <= boundingBox.maxZ() + dest.z() && maxZ() + src.z() >= boundingBox.minZ() + dest.z());
    }

    @Override
    public String toString() {
        String result = "BoundingBox";
        result += "\n";
        result += "[" + minX() + " : " + maxX() + "]";
        result += "\n";
        result += "[" + minY() + " : " + maxY() + "]";
        result += "\n";
        result += "[" + minZ() + " : " + maxZ() + "]";
        return result;
    }

    /**
     * Used to know if this {@link BoundingBox} intersects with the bounding box of an entity.
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    public boolean intersectEntity(@NotNull Point src, @NotNull Entity entity) {
        return intersectBoundingBox(src, entity.getBoundingBox(), entity.getPosition());
    }

    /**
     * Used to know if the bounding box intersects at a block position.
     *
     * @param blockX the block X
     * @param blockY the block Y
     * @param blockZ the block Z
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersectBlock(@NotNull Point src, int blockX, int blockY, int blockZ) {
        final double offsetX = 1;
        final double maxX = (double) blockX + offsetX;
        final boolean checkX = minX() + src.x() < maxX && maxX() + src.x() > (double) blockX;
        if (!checkX) return false;

        final double maxY = (double) blockY + 0.99999;
        final boolean checkY = minY() + src.y() < maxY && maxY() + src.y() > (double) blockY;
        if (!checkY) return false;

        final double offsetZ = 1;
        final double maxZ = (double) blockZ + offsetZ;
        // Z check
        return minZ() + src.z() < maxZ && maxZ() + src.z() > (double) blockZ;
    }

    /**
     * Used to know if the bounding box intersects (contains) a point.
     *
     * @param x x-coord of a point
     * @param y y-coord of a point
     * @param z z-coord of a point
     * @return true if the bounding box intersects (contains) with the point, false otherwise
     */
    public boolean intersectPoint(@NotNull Point src, double x, double y, double z) {
        return (x >= minX() + src.x() && x <= maxX() + src.x()) &&
                (y >= minY() + src.y() && y <= maxY() + src.y()) &&
                (z >= minZ() + src.z() && z <= maxZ() + src.z());
    }

    /**
     * Used to know if the bounding box intersects (contains) a point.
     *
     * @param dest the point to check
     * @return true if the bounding box intersects (contains) with the point, false otherwise
     */
    public boolean intersectPoint(@NotNull Point src, @NotNull Point dest) {
        return intersectPoint(src, dest.x(), dest.y(), dest.z());
    }

    /**
     * Creates a new {@link BoundingBox} linked to the same {@link Entity} with expanded size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new {@link BoundingBox} expanded
     */
    public @NotNull BoundingBox expand(double x, double y, double z) {
        return new BoundingBox(this.width + x, this.height + y, this.depth + z);
    }

    /**
     * Creates a new {@link BoundingBox} linked to the same {@link Entity} with contracted size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public @NotNull BoundingBox contract(double x, double y, double z) {
        return new BoundingBox(this.width - x, this.height - y, this.depth - z) ;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double depth() {
        return depth;
    }

    @NotNull BoundingBoxUtils.Faces faces() {
        return faces;
    }

    public double minX() {
        return -width/2;
    }

    public double maxX() {
        return width/2;
    }

    public double minY() {
        return 0;
    }

    public double maxY() {
        return height;
    }

    public double minZ() {
        return -depth/2;
    }

    public double maxZ() {
        return depth/2;
    }

}
