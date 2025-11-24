package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A ray that can check for collisions along it.
 * <p>
 * You should construct a Ray using {@link #Ray(Point, Vec)}.
 * @param origin the ray's origin
 * @param direction the ray's normalized direction
 * @param distance the maximum distance the ray will check
 * @param inverse the cached inverse of the ray
 */
public record Ray(
        Point origin,
        Vec direction,
        double distance,
        Vec inverse
) {
    /**
     * Constructs a ray.
     * @param origin the origin point
     * @param vector the ray's path, which can have any nonzero length
     */
    public Ray(Point origin, Vec vector) {
        Check.stateCondition(vector.isZero(), "Ray may not have zero length");
        Vec normalized = vector.normalize();
        this(origin, normalized, vector.length(), Vec.ONE.div(normalized));
    }

    /**
     * An intersection found between a {@link Ray} and object of type {@link T}.
     * @param <T> the type of object collided with
     * @param t the distance along the ray that the intersection was found
     * @param point the point of intersection
     * @param normal the normal of the intersected surface
     * @param exitT the distance along the ray that the ray exits the object
     * @param exitPoint the point from which the ray exits the object
     * @param exitNormal the normal of the surface through which the ray exits
     * @param object the object collided with
     */
    public record Intersection<T>(
            double t,
            Point point,
            Vec normal,
            double exitT,
            Point exitPoint,
            Vec exitNormal,
            T object
    ) implements Comparable<Intersection<?>> {
        /**
         * Compares this intersection's t value with that of another one. If they are equal, compares their exitT values.
         * @param o Any other intersection
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(Intersection<?> o) {
            return t != o.t ?
                    (int) Math.signum(t - o.t) :
                    (int) Math.signum(exitT - o.exitT);
        }

        public <R> Intersection<R> withObject(R object) {
            return new Intersection<>(t, point, normal, exitT, exitPoint, exitNormal, object);
        }

        /**
         * Returns whether an intersection overlaps with another; if one's {@link Intersection#exitT} is less than or equal to the other's {@link Intersection#t}.
         * <p>
         * Use this to validate before using {@link Intersection#merge(Intersection)}.
         * @param other the other intersection
         * @return whether the intersections overlap
         */
        public boolean overlaps(Intersection<?> other) {
            return !(other.exitT < t || exitT < other.t);
        }

        /**
         * Merges two intersections by making one out of the lowest t and highest exitT from the intersections.
         * @param other the other intersection
         * @return a potentially larger intersection with the same {@link #object} as this
         */
        public Intersection<T> merge(Intersection<?> other) {
            boolean startsFirst = t < other.t;
            boolean endsLast = exitT >= other.exitT;
            return new Intersection<>(
                    startsFirst ? t : other.t,
                    startsFirst ? point : other.point,
                    startsFirst ? normal : other.normal,
                    endsLast ? exitT : other.exitT,
                    endsLast ? exitPoint : other.exitPoint,
                    endsLast ? exitNormal : other.exitNormal,
                    object
            );
        }
    }

    /**
     * Check if this ray hits some shape.
     * @param shape the shape to check against
     * @param offset an offset to shift the shape by, e.g. for block hitboxes
     * @return an {@link Intersection} if one is found between this ray and the shape, and null otherwise
     * @param <S> any Shape
     */
    public <S extends Shape> @Nullable Intersection<S> cast(S shape, Point offset) {
        Vec bMin = shape.relativeStart().asVec().sub(origin).add(offset);
        Vec bMax = shape.relativeEnd().asVec().sub(origin).add(offset);
        Vec v1 = bMin.mul(inverse);
        Vec v2 = bMax.mul(inverse);

        double tN = Math.min(v1.x(), v2.x());
        double tF = Math.max(v1.x(), v2.x());
        tN = Math.max(tN, Math.min(v1.y(), v2.y()));
        tF = Math.min(tF, Math.max(v1.y(), v2.y()));
        tN = Math.max(tN, Math.min(v1.z(), v2.z()));
        tF = Math.min(tF, Math.max(v1.z(), v2.z()));

        if (tF >= tN && tF >= 0 && tN <= distance) {
            return new Intersection<>(
                    tN,
                    origin.add(direction.mul(tN)),
                    new Vec(
                            -(v1.x() == tN ? 1 : 0) + (v2.x() == tN ? 1 : 0),
                            -(v1.y() == tN ? 1 : 0) + (v2.y() == tN ? 1 : 0),
                            -(v1.z() == tN ? 1 : 0) + (v2.z() == tN ? 1 : 0)
                    ),
                    tF,
                    origin.add(direction.mul(tF)),
                    new Vec(
                            -(v1.x() == tF ? 1 : 0) + (v2.x() == tF ? 1 : 0),
                            -(v1.y() == tF ? 1 : 0) + (v2.y() == tF ? 1 : 0),
                            -(v1.z() == tF ? 1 : 0) + (v2.z() == tF ? 1 : 0)
                    ),
                    shape);
        }

        return null;
    }

    /**
     * Check if this ray hits some shape.
     * <p>
     * If you're checking an {@link Entity}, use {@link Ray#cast(Shape, Point)} with its position.
     * @param shape the shape to check against
     * @return an {@link Intersection} if one is found between this ray and the shape, and null otherwise
     * @param <S> any Shape - for example, a {@link BoundingBox}
     */
    public <S extends Shape> @Nullable Intersection<S> cast(S shape) {
        return cast(shape, Vec.ZERO);
    }

    /**
     * Get an <b>unordered</b> list of collisions with shapes.
     * <p>
     * If you need to know which collisions happened first, use {@link #castSorted(Collection)} or {@link Collections#min(Collection)}.
     * @param shapes the shapes to check against
     * @return a list of results, possibly empty
     * @param <S> any Shape - for example, an {@link net.minestom.server.entity.Entity} or {@link BoundingBox}
     */
    public <S extends Shape> List<Intersection<S>> cast(Collection<? extends S> shapes) {
        ArrayList<Intersection<S>> result = new ArrayList<>(shapes.size());
        for (S e: shapes) {
            Intersection<S> r = cast(e);
            if (r != null) result.add(r);
        }
        return result;
    }

    /**
     * Get an ordered list of collisions with shapes, starting with the closest to the ray origin.
     * @param shapes the shapes to check against
     * @return a list of results, possibly empty
     * @param <S> any Shape - for example, a {@link BoundingBox}
     */
    public <S extends Shape> List<Intersection<S>> castSorted(Collection<? extends S> shapes) {
        ArrayList<Intersection<S>> result = new ArrayList<>(shapes.size());
        for (S e: shapes) {
            Intersection<S> r = cast(e);
            if (r != null) result.add(r);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Get the closest collision to the ray's origin.
     * @param shapes the shapes to check against
     * @return the closest result or null if there is none
     * @param <S> any Shape - for example, a{@link BoundingBox}
     */
    public <S extends Shape> @Nullable Intersection<S> findFirst(Collection<? extends S> shapes) {
        ArrayList<Intersection<S>> result = new ArrayList<>(shapes.size());
        for (S e: shapes) {
            Intersection<S> r = cast(e);
            if (r != null) result.add(r);
        }
        if (result.isEmpty()) return null;
        return Collections.min(result);
    }

    /**
     * Get an <b>unordered</b> list of collisions with entities.
     * <p>
     * If you need to know which collisions happened first, use {@link #entitiesSorted(Collection)} or {@link Collections#min(Collection)}.
     * @param entities the entities to check against
     * @return a list of results, possibly empty
     * @param <E> any Entity - if you're using {@link net.minestom.server.instance.EntityTracker}, you might use {@link net.minestom.server.entity.Player}
     */
    public <E extends Entity> List<Intersection<E>> entities(Collection<? extends E> entities) {
        ArrayList<Intersection<E>> result = new ArrayList<>(entities.size());
        for (E e: entities) {
            Intersection<E> r = cast(e, e.getPosition().asVec());
            if (r != null) result.add(r);
        }
        return result;
    }

    /**
     * Get an ordered list of collisions with entities, starting with the closest to the ray origin.
     * @param entities the entities to check against
     * @return a list of results, possibly empty
     * @param <E> any Entity - if you're using {@link net.minestom.server.instance.EntityTracker}, you might use {@link net.minestom.server.entity.Player}
     */
    public <E extends Entity> List<Intersection<E>> entitiesSorted(Collection<? extends E> entities) {
        ArrayList<Intersection<E>> result = new ArrayList<>(entities.size());
        for (E e: entities) {
            Intersection<E> r = cast(e, e.getPosition().asVec());
            if (r != null) result.add(r);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Gets a {@link BlockIterator} along this ray.
     * @return a {@link BlockIterator}
     */
    public BlockIterator blockIterator() {
        return new BlockIterator(origin.asVec(), direction, 0, distance);
    }

    /**
     * Gets a {@link BlockFinder} along this ray.
     * <p>
     * This is useful if you need only the first hit point, for instance, as it does not perform merging.
     * @param blockGetter the provider for blocks, such as an {@link net.minestom.server.instance.Instance} or {@link net.minestom.server.instance.Chunk}
     * @return a {@link BlockFinder}
     */
    public BlockFinder findBlocks(Block.Getter blockGetter) {
        return new BlockFinder(this, blockIterator(), blockGetter, BlockFinder.SOLID_BLOCK_HITBOXES);
    }

    /**
     * Gets a {@link BlockFinder} along this ray.
     * <p>
     * This is useful if you need only the first hit point, for instance, as it does not perform merging.
     * @param blockGetter the provider for blocks, such as an {@link net.minestom.server.instance.Instance} or {@link net.minestom.server.instance.Chunk}
     * @param hitboxGetter a function that gets bounding boxes from a block
     *                     <p>
     *                     {@link BlockFinder} provides some options, and {@link BlockFinder#SOLID_BLOCK_HITBOXES} is the default.
     * @return a {@link BlockFinder}
     */
    public BlockFinder findBlocks(Block.Getter blockGetter, Function<Block, Collection<BoundingBox>> hitboxGetter) {
        return new BlockFinder(this, blockIterator(), blockGetter, hitboxGetter);
    }

    /**
     * Gets a {@link BlockQueue} along this ray.
     * <p>
     * These can perform merging. They are useful if you need exit points from blocks.
     * @param blockGetter the provider for blocks, such as an {@link net.minestom.server.instance.Instance} or {@link net.minestom.server.instance.Chunk}
     * @return a {@link BlockQueue}
     */
    public BlockQueue blockQueue(Block.Getter blockGetter) {
        return new BlockQueue(findBlocks(blockGetter));
    }

    /**
     * Gets a {@link BlockQueue} along this ray.
     * <p>
     * These can perform merging. They are useful if you need exit points from blocks.
     * @param blockGetter the provider for blocks, such as an {@link net.minestom.server.instance.Instance} or {@link net.minestom.server.instance.Chunk}
     * @param hitboxGetter a function that gets bounding boxes from a block
     *                     <p>
     *                     {@link BlockFinder} provides some options, and {@link BlockFinder#SOLID_BLOCK_HITBOXES} is the default.
     * @return a {@link BlockQueue}
     */
    public BlockQueue blockQueue(Block.Getter blockGetter, Function<Block, Collection<BoundingBox>> hitboxGetter) {
        return new BlockQueue(findBlocks(blockGetter, hitboxGetter));
    }

    /**
     * Gets the end point of this ray with some data that may or may not be useful.
     * @return the end point as a result
     */
    public Intersection<Ray> endPoint() {
        return new Intersection<>(distance, origin.add(direction.mul(distance)), direction.neg(), distance, origin.add(direction.mul(distance)), direction, this);
    }
}
