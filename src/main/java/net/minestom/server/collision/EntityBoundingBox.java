package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
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
public class EntityBoundingBox implements Collidable {
    private final double width, height, depth;
    private final Faces faces;

    public boolean intersectBlock(Point src, Block block, Point dest) {
        return block.registry().boundingBoxes().intersectEntity(src, this, dest);
    }

    public List<? extends Collidable> intersectBlockSwept(Point rayStart, Point rayDirection, Block block, Point blockPos) {
        return block.registry().boundingBoxes().intersectEntitySwept(rayStart, rayDirection, blockPos, this);
    }

    public EntityBoundingBox(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.faces = retrieveFaces();
    }

    /**
     * Used to know if two {@link EntityBoundingBox} intersect with each other.
     *
     * @param entityBoundingBox the {@link EntityBoundingBox} to check
     * @return true if the two {@link EntityBoundingBox} intersect with each other, false otherwise
     */
    public boolean intersectCollidable(@NotNull Point src, @NotNull Collidable entityBoundingBox, @NotNull Point dest) {
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
     * Used to know if this {@link EntityBoundingBox} intersects with the bounding box of an entity.
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    public boolean intersectEntity(@NotNull Point src, @NotNull Entity entity) {
        return intersectCollidable(src, entity.getBoundingBox(), entity.getPosition());
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
     * Creates a new {@link EntityBoundingBox} linked to the same {@link Entity} with expanded size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new {@link EntityBoundingBox} expanded
     */
    public @NotNull EntityBoundingBox expand(double x, double y, double z) {
        return new EntityBoundingBox(this.width + x, this.height + y, this.depth + z);
    }

    /**
     * Creates a new {@link EntityBoundingBox} linked to the same {@link Entity} with contracted size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public @NotNull EntityBoundingBox contract(double x, double y, double z) {
        return new EntityBoundingBox(this.width - x, this.height - y, this.depth - z) ;
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
        return -width / 2;
    }

    public double maxX() {
        return width / 2;
    }

    public double minY() {
        return 0;
    }

    public double maxY() {
        return height;
    }

    public double minZ() {
        return -depth / 2;
    }

    public double maxZ() {
        return depth / 2;
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
