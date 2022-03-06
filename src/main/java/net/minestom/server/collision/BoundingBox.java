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
public class BoundingBox {
    private final double width, height, depth;
    private final Faces faces;
    private final BoundingBoxType type;

    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public enum BoundingBoxType {
        ENTITY, BLOCK
    }

    public BoundingBox(double width, double height, double depth, BoundingBoxType type) {
        this(width, height, depth, type, -(width / 2), 0, -(depth / 2), (width / 2), height, (depth / 2));
    }

    public BoundingBox(double width, double height, double depth, BoundingBoxType type, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.type = type;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;

        if (this.type == BoundingBoxType.ENTITY)
            this.faces = retrieveFaces();
        else this.faces = null;
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
     * Used to know if the bounding box intersects at a point.
     *
     * @param blockPosition the position to check
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersectBlock(@NotNull Point src, @NotNull Point blockPosition) {
        return intersectBlock(src, blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());
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
     * Used to know if the bounding box intersects a line segment.
     *
     * @param x1 x-coord of first line segment point
     * @param y1 y-coord of first line segment point
     * @param z1 z-coord of first line segment point
     * @param x2 x-coord of second line segment point
     * @param y2 y-coord of second line segment point
     * @param z2 z-coord of second line segment point
     * @return true if the bounding box intersects with the line segment, false otherwise.
     */
    public boolean intersectLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        // originally from http://www.3dkingdoms.com/weekly/weekly.php?a=3
        double x3 = minX();
        double x4 = maxX();
        double y3 = minY();
        double y4 = maxY();
        double z3 = minZ();
        double z4 = maxZ();
        if (x1 > x3 && x1 < x4 && y1 > y3 && y1 < y4 && z1 > z3 && z1 < z4) {
            return true;
        }
        if (x1 < x3 && x2 < x3 || x1 > x4 && x2 > x4 ||
                y1 < y3 && y2 < y3 || y1 > y4 && y2 > y4 ||
                z1 < z3 && z2 < z3 || z1 > z4 && z2 > z4) {
            return false;
        }
        return isInsideBoxWithAxis(Axis.X, getSegmentIntersection(x1 - x3, x2 - x3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.X, getSegmentIntersection(x1 - x4, x2 - x4, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Y, getSegmentIntersection(y1 - y3, y2 - y3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Y, getSegmentIntersection(y1 - y4, y2 - y4, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Z, getSegmentIntersection(z1 - z3, z2 - z3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Z, getSegmentIntersection(z1 - z4, z2 - z4, x1, y1, z1, x2, y2, z2));
    }

    /**
     * Used to know if the bounding box intersects a line segment.
     *
     * @param lineStart first line segment point
     * @param lineEnd   second line segment point
     * @return true if the bounding box intersects with the line segment, false otherwise.
     */
    public boolean intersectLine(@NotNull Point lineStart, @NotNull Point lineEnd) {
        return intersectLine(
                Math.min(lineStart.x(), lineEnd.x()),
                Math.min(lineStart.y(), lineEnd.y()),
                Math.min(lineStart.z(), lineEnd.z()),
                Math.max(lineStart.x(), lineEnd.x()),
                Math.max(lineStart.y(), lineEnd.y()),
                Math.max(lineStart.z(), lineEnd.z())
        );
    }

    private @Nullable Vec getSegmentIntersection(double dst1, double dst2, double x1, double y1, double z1, double x2, double y2, double z2) {
        if (dst1 == dst2 || dst1 * dst2 >= 0D) return null;
        final double delta = dst1 / (dst1 - dst2);
        return new Vec(
                x1 + (x2 - x1) * delta,
                y1 + (y2 - y1) * delta,
                z1 + (z2 - z1) * delta
        );
    }

    private boolean isInsideBoxWithAxis(Axis axis, @Nullable Vec intersection) {
        if (intersection == null) return false;
        double x1 = minX();
        double x2 = maxX();
        double y1 = minY();
        double y2 = maxY();
        double z1 = minZ();
        double z2 = maxZ();
        return axis == Axis.X && intersection.z() > z1 && intersection.z() < z2 && intersection.y() > y1 && intersection.y() < y2 ||
                axis == Axis.Y && intersection.z() > z1 && intersection.z() < z2 && intersection.x() > x1 && intersection.x() < x2 ||
                axis == Axis.Z && intersection.x() > x1 && intersection.x() < x2 && intersection.y() > y1 && intersection.y() < y2;
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
        return new BoundingBox(this.width + x, this.height + y, this.depth + z, type);
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
        return new BoundingBox(this.width - x, this.height - y, this.depth - z, type) ;
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

    @NotNull Faces faces() {
        return faces;
    }

    public double minX() {
        return minX;
    }

    public double maxX() {
        return maxX;
    }

    public double minY() {
        return minY;
    }

    public double maxY() {
        return maxY;
    }

    public double minZ() {
        return minZ;
    }

    public double maxZ() {
        return maxZ;
    }

    private enum Axis {
        X, Y, Z
    }

    record Faces(Map<Vec, List<Vec>> query) {
        public Faces {
            query = Map.copyOf(query);
        }
    }

    private List<Vec> buildSet(Set<Vec> a) {
        return a.stream().toList();
    }

    private List<Vec> buildSet(Set<Vec> a, Set<Vec> b) {
        Set<Vec> allFaces = new HashSet<>();
        Stream.of(a, b).forEach(allFaces::addAll);
        return allFaces.stream().toList();
    }

    private List<Vec> buildSet(Set<Vec> a, Set<Vec> b, Set<Vec> c) {
        Set<Vec> allFaces = new HashSet<>();
        Stream.of(a, b, c).forEach(allFaces::addAll);
        return allFaces.stream().toList();
    }

    private Faces retrieveFaces() {
        double minX = minX();
        double maxX = maxX();
        double minY = minY();
        double maxY = maxY();
        double minZ = minZ();
        double maxZ = maxZ();

        // Calculate steppings for each axis
        // Start at minimum, increase by step size until we reach maximum
        // This is done to catch all blocks that are part of that axis
        // Since this stops before max point is reached, we add the max point after
        final List<Double> stepsX = IntStream.rangeClosed(0, (int)((maxX-minX))).mapToDouble(x -> x + minX).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsY = IntStream.rangeClosed(0, (int)((maxY-minY))).mapToDouble(x -> x + minY).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsZ = IntStream.rangeClosed(0, (int)((maxZ-minZ))).mapToDouble(x -> x + minZ).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));

        stepsX.add(maxX);
        stepsY.add(maxY);
        stepsZ.add(maxZ);

        final Set<Vec> bottom = new HashSet<>();
        final Set<Vec> top = new HashSet<>();
        final Set<Vec> left = new HashSet<>();
        final Set<Vec> right = new HashSet<>();
        final Set<Vec> front = new HashSet<>();
        final Set<Vec> back = new HashSet<>();

        CartesianProduct.product(stepsX, stepsY).forEach(cross -> {
            double i = (double) ((List<?>)cross).get(0);
            double j = (double) ((List<?>)cross).get(1);
            front.add(new Vec(i, j, minZ));
            back.add(new Vec(i, j, maxZ));
        });

        CartesianProduct.product(stepsY, stepsZ).forEach(cross -> {
            double j = (double) ((List<?>)cross).get(0);
            double k = (double) ((List<?>)cross).get(1);
            left.add(new Vec(minX, j, k));
            right.add(new Vec(maxX, j, k));
        });

        CartesianProduct.product(stepsX, stepsZ).forEach(cross -> {
            double i = (double) ((List<?>)cross).get(0);
            double k = (double) ((List<?>)cross).get(1);
            bottom.add(new Vec(i, minY, k));
            top.add(new Vec(i, maxY, k));
        });

        // X   -1 left    |  1 right
        // Y   -1 bottom  |  1 top
        // Z   -1 front   |  1 back
        var query = new HashMap<Vec, List<Vec>>();
        query.put(new Vec(0, 0, 0), List.of());

        query.put(new Vec(-1, 0, 0), buildSet(left));
        query.put(new Vec(1, 0, 0), buildSet(right));
        query.put(new Vec(0, -1, 0), buildSet(bottom));
        query.put(new Vec(0, 1, 0), buildSet(top));
        query.put(new Vec(0, 0, -1), buildSet(front));
        query.put(new Vec(0, 0, 1), buildSet(back));

        query.put(new Vec(0, -1, -1), buildSet(bottom, front));
        query.put(new Vec(0, -1, 1), buildSet(bottom, back));
        query.put(new Vec(0, 1, -1), buildSet(top, front));
        query.put(new Vec(0, 1, 1), buildSet(top, back));

        query.put(new Vec(-1, -1, 0), buildSet(left, bottom));
        query.put(new Vec(-1, 1, 0), buildSet(left, top));
        query.put(new Vec(1, -1, 0), buildSet(right, bottom));
        query.put(new Vec(1, 1, 0), buildSet(right, top));

        query.put(new Vec(-1, 0, -1), buildSet(left, front));
        query.put(new Vec(-1, 0, 1), buildSet(left, back));
        query.put(new Vec(1, 0, -1), buildSet(right, front));
        query.put(new Vec(1, 0, 1), buildSet(right, back));

        query.put(new Vec(1, 1, 1), buildSet(right, top, back));
        query.put(new Vec(1, 1, -1), buildSet(right, top, front));
        query.put(new Vec(1, -1, 1), buildSet(right, bottom, back));
        query.put(new Vec(1, -1, -1), buildSet(right, bottom, front));
        query.put(new Vec(-1, 1, 1), buildSet(left, top, back));
        query.put(new Vec(-1, 1, -1), buildSet(left, top, front));
        query.put(new Vec(-1, -1, 1), buildSet(left, bottom, back));
        query.put(new Vec(-1, -1, -1), buildSet(left, bottom, front));

        return new Faces(query);
    }
}
