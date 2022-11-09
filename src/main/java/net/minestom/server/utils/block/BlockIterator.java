package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {
    private final Vec direction;
    private final Point start;

    private final Point[] points = new Point[3];
    private final double[] distances = new double[3];
    private final int[] signums = new int[3];

    private final Vec end;
    private boolean foundEnd = false;

    private final ArrayDeque<Point> extraPoints = new ArrayDeque<>();

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

        var startSmall = start.add(0, yOffset, 0).sub(direction.normalize().mul(0.01)).apply(Vec.Operator.FLOOR);
        calculateIntersectionX(startSmall, direction, signums[0] > 0 ? 1 : 0);
        calculateIntersectionY(startSmall, direction, signums[1] > 0 ? 1 : 0);
        calculateIntersectionZ(startSmall, direction, signums[2] > 0 ? 1 : 0);

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
        var res = extraPoints.isEmpty() ? updateClosest() : extraPoints.poll();
        return new Vec(res.blockX(), res.blockY(), res.blockZ());
    }

    private void calculateIntersectionX(Point start, Vec direction, int signum) {
        double x = Math.floor(start.x()) + signum;
        double y = start.y() + (x - start.x()) * direction.y() / direction.x();
        double z = start.z() + (x - start.x()) * direction.z() / direction.x();
        points[0] = new Vec(x, y, z);
        distances[0] = this.start.distance(points[0]);
    }

    private void calculateIntersectionY(Point start, Vec direction, int signum) {
        double y = Math.floor(start.y()) + signum;
        double x = start.x() + (y - start.y()) * direction.x() / direction.y();
        double z = start.z() + (y - start.y()) * direction.z() / direction.y();
        points[1] = new Vec(x, y, z);
        distances[1] = this.start.distance(points[1]);
    }

    private void calculateIntersectionZ(Point start, Vec direction, int signum) {
        double z = Math.floor(start.z()) + signum;
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

        int sub[] = new int[3];

        boolean needsX = distances[0] == minDistance;
        boolean needsY = distances[1] == minDistance;
        boolean needsZ = distances[2] == minDistance;

        Point closest = null;
        if (needsX) {
            closest = points[0];
            if (signums[0] == 1) sub[0] = 1;
            calculateIntersectionX(points[0], direction, signums[0]);
        }
        if (needsY) {
            closest = points[1];
            if (signums[1] == 1) sub[1] = 1;
            calculateIntersectionY(points[1], direction, signums[1]);
        }
        if (needsZ) {
            closest = points[2];
            if (signums[2] == 1) sub[2] = 1;
            calculateIntersectionZ(points[2], direction, signums[2]);
        }

        closest = closest.sub(sub[0], sub[1], sub[2]);

        if (needsX && needsY && needsZ) {
            extraPoints.add(closest.add(signums[0], 0, 0));
            extraPoints.add(closest.add(0, signums[1], 0));
            extraPoints.add(closest.add(0, 0, signums[2]));
        } else if (needsX && needsY) {
            extraPoints.add(closest.add(signums[0], 0, 0));
            extraPoints.add(closest.add(0, signums[1], 0));
        } else if (needsX && needsZ) {
            extraPoints.add(closest.add(signums[0], 0, 0));
            extraPoints.add(closest.add(0, 0, signums[2]));
        } else if (needsY && needsZ) {
            extraPoints.add(closest.add(0, signums[1], 0));
            extraPoints.add(closest.add(0, 0, signums[2]));
        }

        if (closest.sameBlock(end)) foundEnd = true;

        return closest;
    }
}
