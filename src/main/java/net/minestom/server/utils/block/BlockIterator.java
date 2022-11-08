package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {
    private final Vec direction;
    private final Point start;

    private final Point[] points = new Point[3];
    private final double[] distances = new double[3];
    private final int[] signums = new int[3];

    private boolean foundEnd;
    private final Point end;

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
        this.direction = direction;
        this.start = start.add(0, yOffset, 0);
        this.end = start.add(0, yOffset, 0).add(direction.normalize().mul(maxDistance)).apply(Vec.Operator.FLOOR);

        signums[0] = (int) Math.signum(direction.x());
        signums[1] = (int) Math.signum(direction.y());
        signums[2] = (int) Math.signum(direction.z());

        points[0] = start;
        points[1] = start;
        points[2] = start;

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
        System.out.println("POINTS " + points[0] + " " + points[1] + " " + points[2]);
        System.out.println("DISTANCES " + distances[0] + " " + distances[1] + " " + distances[2]);
        return !foundEnd;
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
        var res = updateClosest();
        if (end.sameBlock(res)) {
            foundEnd = true;
        }

        return res;
    }

    private void calculateIntersectionX(Point start, Vec direction) {
        double x = Math.floor(start.x());
        if (x == start.x() || signums[0] > 0) x += signums[0];

        double y = start.y() + (x - start.x()) * direction.y() / direction.x();
        double z = start.z() + (x - start.x()) * direction.z() / direction.x();
        points[0] = new Vec(x, y, z);
        distances[0] = this.start.distance(points[0]);
    }

    private void calculateIntersectionY(Point start, Vec direction) {
        double y = Math.floor(start.y());
        if (y == start.y() || signums[1] > 0) y += signums[1];

        double x = start.x() + (y - start.y()) * direction.x() / direction.y();
        double z = start.z() + (y - start.y()) * direction.z() / direction.y();
        points[1] = new Vec(x, y, z);
        distances[1] = this.start.distance(points[1]);
    }

    private void calculateIntersectionZ(Point start, Vec direction) {
        double z = Math.floor(start.z());
        if (z == start.z() || signums[2] > 0) z += signums[2];

        double x = start.x() + (z - start.z()) * direction.x() / direction.z();
        double y = start.y() + (z - start.z()) * direction.y() / direction.z();
        points[2] = new Vec(x, y, z);
        distances[2] = this.start.distance(points[2]);
    }

    private Point fixX(Point point) {
        System.out.println("X " + point);
        int x = (int) Math.floor(point.x());
        if (x == point.x() && signums[0] == 1) x--;
        return new Vec(x, point.blockY(), point.blockZ());
    }

    private Point fixY(Point point) {
        System.out.println("Y " + point);
        int y = (int) Math.floor(point.y());
        if (y == point.y() && signums[1] == 1) y--;
        return new Vec(point.blockX(), y, point.blockZ());
    }

    private Point fixZ(Point point) {
        System.out.println("Z " + point);
        int z = (int) Math.floor(point.z());
        if (z == point.z() && signums[2] == 1) z--;
        return new Vec(point.blockX(), point.blockY(), z);
    }

    private Point updateClosest() {
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            if (distances[i] < minDistance) {
                minDistance = distances[i];
            }
        }

        // Update the closest grid intersections
        // If multiple points are the same, we can update them all
        if(distances[0] == minDistance) {
            var res = fixX(points[0]);
            calculateIntersectionX(points[0], direction);
            return res;
        }

        if(distances[1] == minDistance) {
            var res = fixY(points[1]);
            calculateIntersectionY(points[1], direction);
            return res;
        }

        if(distances[2] == minDistance) {
            var res = fixZ(points[2]);
            calculateIntersectionZ(points[2], direction);
            return res;
        }

        return null;
    }
}
