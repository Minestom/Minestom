package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public final class BoundingBox implements Shape {
    private final double width, height, depth;
    Point offset;
    final Faces faces;

    public boolean intersectBlock(Point src, Block block, Point dest) {
        return block.registry().shape().intersectEntity(src, this, dest);
    }

    public boolean intersectBlockSwept(Point entityPosition, Point rayDirection, Block block, Point blockPos, SweepResult tempResult, SweepResult finalResult) {
        Point rayStart = entityPosition.add(0, height() / 2, 0);
        return block.registry().shape().intersectEntitySwept(rayStart, rayDirection, blockPos, this, entityPosition, tempResult, finalResult);
    }

    public BoundingBox(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.offset = new Vec(-width / 2, 0, -depth / 2);
        this.faces = retrieveFaces();
    }

    @Override
    public boolean intersectEntity(Point position, BoundingBox boundingBox, Point placementPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, Point entityPosition, SweepResult tempResult, SweepResult finalResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Point relativeStart() {
        return offset;
    }

    @Override
    public @NotNull Point relativeEnd() {
        return offset.add(width, height, depth);
    }

    /**
     * Used to know if two {@link BoundingBox} intersect with each other.
     *
     * @param entityBoundingBox the {@link BoundingBox} to check
     * @return true if the two {@link BoundingBox} intersect with each other, false otherwise
     */
    public boolean intersectCollidable(@NotNull Point src, @NotNull BoundingBox entityBoundingBox, @NotNull Point dest) {
        return (minX() + src.x() <= entityBoundingBox.maxX() + dest.x() && maxX() + src.x() >= entityBoundingBox.minX() + dest.x()) &&
                (minY() + src.y() <= entityBoundingBox.maxY() + dest.y() && maxY() + src.y() >= entityBoundingBox.minY() + dest.y()) &&
                (minZ() + src.z() <= entityBoundingBox.maxZ() + dest.z() && maxZ() + src.z() >= entityBoundingBox.minZ() + dest.z());
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
        return intersectCollidable(src, entity.getBoundingBox(), entity.getPosition());
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
        return new BoundingBox(this.width - x, this.height - y, this.depth - z);
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
        return relativeStart().x();
    }

    public double maxX() {
        return relativeEnd().x();
    }

    public double minY() {
        return relativeStart().y();
    }

    public double maxY() {
        return relativeEnd().y();
    }

    public double minZ() {
        return relativeStart().z();
    }

    public double maxZ() {
        return relativeEnd().z();
    }

    public boolean BoundingBoxRayIntersectionCheck(Vec start, Vec direction, Pos position) {
        return RayUtils.BoundingBoxRayIntersectionCheck(start, direction, this, position);
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
        final List<Double> stepsX = IntStream.rangeClosed(0, (int) ((maxX - minX))).mapToDouble(x -> x + minX).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsY = IntStream.rangeClosed(0, (int) ((maxY - minY))).mapToDouble(x -> x + minY).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));
        final List<Double> stepsZ = IntStream.rangeClosed(0, (int) ((maxZ - minZ))).mapToDouble(x -> x + minZ).boxed().collect(Collectors.toCollection(ArrayList<Double>::new));

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
            double i = (double) ((List<?>) cross).get(0);
            double j = (double) ((List<?>) cross).get(1);
            front.add(new Vec(i, j, minZ));
            back.add(new Vec(i, j, maxZ));
        });

        CartesianProduct.product(stepsY, stepsZ).forEach(cross -> {
            double j = (double) ((List<?>) cross).get(0);
            double k = (double) ((List<?>) cross).get(1);
            left.add(new Vec(minX, j, k));
            right.add(new Vec(maxX, j, k));
        });

        CartesianProduct.product(stepsX, stepsZ).forEach(cross -> {
            double i = (double) ((List<?>) cross).get(0);
            double k = (double) ((List<?>) cross).get(1);
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
