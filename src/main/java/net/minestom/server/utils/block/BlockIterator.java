package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {
    private final double maxDistance;
    private final Vec direction;
    private final Point start;

    private Point[] points = new Point[3];
    private double[] distances = new double[3];
    private int[] signums = new int[3];

    private Point lastClosest;

    private boolean lastBlockSent = false;
    private final Point end;

    ArrayDeque<Point> scuffedQueue = new ArrayDeque<>();

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param start       A Vector giving the initial position for the trace
     * @param direction   A Vector pointing in the direction for the trace
     * @param yOffset     The trace begins vertically offset from the start vector
     *                    by this value
     * @param maxDistance This is the maximum distance in blocks for the
     *                    trace. Setting this value above 140 may lead to problems with
     *                    unloaded chunks. A value of 0 indicates no limit
     */
    public BlockIterator(@NotNull Vec start, @NotNull Vec direction, double yOffset, double maxDistance) {
        this.maxDistance = maxDistance;
        this.direction = direction;
        this.start = start.add(0, yOffset, 0);
        this.end = start.add(0, yOffset, 0).add(direction.normalize().mul(maxDistance)).apply(Vec.Operator.FLOOR);
        this.lastClosest = start.add(0, yOffset, 0).sub(direction.normalize().mul(Vec.EPSILON)).apply(Vec.Operator.FLOOR);

        points[0] = this.start;
        points[1] = this.start;
        points[2] = this.start;

        distances[0] = 0;
        distances[1] = 0;
        distances[2] = 0;

        if (direction.x() == 0) {
            points[0] = null;
            distances[0] = Double.MAX_VALUE;
        }
        if (direction.y() == 0) {
            points[1] = null;
            distances[1] = Double.MAX_VALUE;
        }
        if (direction.z() == 0) {
            points[2] = null;
            distances[2] = Double.MAX_VALUE;
        }

        signums[0] = (int) Math.signum(direction.x());
        signums[1] = (int) Math.signum(direction.y());
        signums[2] = (int) Math.signum(direction.z());
    }

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param pos         The position for the start of the ray trace
     * @param yOffset     The trace begins vertically offset from the start vector
     *                    by this value
     * @param maxDistance This is the maximum distance in blocks for the
     *                    trace. Setting this value above 140 may lead to problems with
     *                    unloaded chunks. A value of 0 indicates no limit
     */

    public BlockIterator(@NotNull Pos pos, double yOffset, int maxDistance) {
        this(pos.asVec(), pos.direction(), yOffset, maxDistance);
    }

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param pos     The position for the start of the ray trace
     * @param yOffset The trace begins vertically offset from the start vector
     *                by this value
     */

    public BlockIterator(@NotNull Pos pos, double yOffset) {
        this(pos.asVec(), pos.direction(), yOffset, 0);
    }

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param pos The position for the start of the ray trace
     */

    public BlockIterator(@NotNull Pos pos) {
        this(pos, 0f);
    }

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param entity      Information from the entity is used to set up the trace
     * @param maxDistance This is the maximum distance in blocks for the
     *                    trace. Setting this value above 140 may lead to problems with
     *                    unloaded chunks. A value of 0 indicates no limit
     */

    public BlockIterator(@NotNull Entity entity, int maxDistance) {
        this(entity.getPosition(), entity.getEyeHeight(), maxDistance);
    }

    /**
     * Constructs the BlockIterator.
     * <p>
     * This considers all blocks as 1x1x1 in size.
     *
     * @param entity Information from the entity is used to set up the trace
     */

    public BlockIterator(@NotNull Entity entity) {
        this(entity, 0);
    }

    /**
     * Returns true if the iteration has more elements
     */

    @Override
    public boolean hasNext() {
        return !lastBlockSent;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("[BlockIterator] doesn't support block removal");
    }

    /**
     * Returns the next BlockPosition in the trace
     *
     * @return the next BlockPosition in the trace
     */

    @Override
    public Point next() {
        if (!scuffedQueue.isEmpty()) return scuffedQueue.poll();
        Point closest = updateClosest();

        int x = (int) Math.floor(closest.x());
        int y = (int) Math.floor(closest.y());
        int z = (int) Math.floor(closest.z());

        // When moving in the positive direction, you want the previous block
        if (z == closest.z() && signums[2] == 1 && z != closest.z()) z--;
        if (x == closest.x() && signums[0] == 1 && x != closest.x()) x--;
        if (y == closest.y() && signums[1] == 1 && y != closest.y()) y--;

        Vec res = new Vec(x, y, z);

        getInbetween(lastClosest, closest);
        lastClosest = res;

        if (res.equals(end)) lastBlockSent = true;

        System.out.println("Queue " + scuffedQueue);

        return scuffedQueue.poll();
    }

    private void calculateIntersectionX(Point start, Vec direction) {
        double x = Math.floor(start.x()) + signums[0];
        double y = start.y() + (x - start.x()) * direction.y() / direction.x();
        double z = start.z() + (x - start.x()) * direction.z() / direction.x();
        points[0] = new Vec(x, y, z);
        distances[0] = this.start.distance(points[0]);
    }

    private void calculateIntersectionY(Point start, Vec direction) {
        double y = Math.floor(start.y()) + signums[1];
        double x = start.x() + (y - start.y()) * direction.x() / direction.y();
        double z = start.z() + (y - start.y()) * direction.z() / direction.y();
        points[1] = new Vec(x, y, z);
        distances[1] = this.start.distance(points[1]);
    }

    private void calculateIntersectionZ(Point start, Vec direction) {
        double z = Math.floor(start.z()) + signums[2];
        double x = start.x() + (z - start.z()) * direction.x() / direction.z();
        double y = start.y() + (z - start.z()) * direction.y() / direction.z();
        points[2] = new Vec(x, y, z);
        distances[2] = this.start.distance(points[2]);
    }

    private Point updateClosest() {
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            if (distances[i] < minDistance) {
                minDistance = distances[i];
            }
        }

        if (scuffedQueue.size() > 8) {
            throw new IndexOutOfBoundsException("[BlockIterator] Queue is too big");
        }

        // Update the closest grid intersections
        // If multiple points are the same, we can update them all
        Point closest = null;
        if(distances[0] == minDistance) {
            closest = points[0];
            calculateIntersectionX(points[0], direction);
        }
        if(distances[1] == minDistance) {
            closest = points[1];
            calculateIntersectionY(points[1], direction);
        }
        if(distances[2] == minDistance) {
            closest = points[2];
            calculateIntersectionZ(points[2], direction);
        }

        return closest;
    }

    private void getInbetween(Point pointBefore, Point pointAfter) {
        System.out.println("Point before " + pointBefore);
        System.out.println("Point after " + pointAfter);

        if (pointBefore.sameBlock(pointAfter))
            scuffedQueue.add(new Vec(pointAfter.blockX(), pointAfter.blockY(), pointAfter.blockZ()));

        if (pointAfter.blockX() != pointBefore.blockX() && pointAfter.x() == pointAfter.blockX()) {
            // Pass through (+1, 0, 0)
            scuffedQueue.add(new Vec(pointBefore.blockX(), pointAfter.blockY(), pointAfter.blockZ()));

            // Checks for moving through 4 blocks
            if (pointAfter.blockY() != pointBefore.blockY() && pointAfter.y() == pointAfter.blockY()) {
                // Pass through (+1, +1, 0)
                scuffedQueue.add(new Vec(pointBefore.blockX(), pointBefore.blockY(), pointAfter.blockZ()));
            }

            if (pointAfter.blockZ() != pointBefore.blockZ() && pointAfter.z() == pointAfter.blockZ()) {
                // Pass through (+1, 0, +1)
                scuffedQueue.add(new Vec(pointBefore.blockX(), pointAfter.blockY(), pointBefore.blockZ()));
            }
        }

        if (pointAfter.blockY() != pointBefore.blockY() && pointAfter.y() == pointAfter.blockY()) {
            // Pass through (0, +1, 0)
            scuffedQueue.add(new Vec(pointAfter.blockX(), pointBefore.blockY(), pointAfter.blockZ()));

            // Checks for moving through 4 blocks
            if (pointAfter.blockZ() != pointBefore.blockZ() && pointAfter.z() == pointAfter.blockZ()) {
                // Pass through (0, +1, +1)
                scuffedQueue.add(new Vec(pointAfter.blockX(), pointBefore.blockY(), pointBefore.blockZ()));
            }
        }

        if (pointAfter.blockZ() != pointBefore.blockZ() && pointAfter.z() == pointAfter.blockZ()) {
            // Pass through (0, 0, +1)
            scuffedQueue.add(new Vec(pointAfter.blockX(), pointAfter.blockY(), pointBefore.blockZ()));
        }
    }
}
