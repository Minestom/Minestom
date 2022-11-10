package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {
    private final Vec direction;
    private final Point start;

    private final Point[] points = new Point[3];
    private final double[] distances = new double[3];
    private final short[] signums = new short[3];
    private final int[] steps = new int[3];

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

        if (this.direction.isZero()) this.foundEnd = true;

        signums[0] = (short) Math.signum(direction.x());
        signums[1] = (short) Math.signum(direction.y());
        signums[2] = (short) Math.signum(direction.z());

        steps[0] = start.blockX();
        steps[1] = start.blockY();
        steps[2] = start.blockZ();

        System.out.println(steps[0] + " " + steps[1] + " " + steps[2]);

        // Find grid intersections for x, y, z
        // This works by calculating and storing the distance to the next grid intersection on the x, y and z axis
        // On every iteration, we return the nearest grid intersection and update it
        calculateIntersectionX(start, direction, signums[0] > 0 ? 1 : 0);
        calculateIntersectionY(start, direction, signums[1] > 0 ? 1 : 0);
        calculateIntersectionZ(start, direction, signums[2] > 0 ? 1 : 0);

        // If directions are 0, set distances to max to stop the intersection point from being used
        if (direction.x() == 0) distances[0] = Double.MAX_VALUE;
        if (direction.y() == 0) distances[1] = Double.MAX_VALUE;
        if (direction.z() == 0) distances[2] = Double.MAX_VALUE;
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
        if (foundEnd) throw new NoSuchElementException();

        // If we have entries in the extra points queue, return those first
        var res = extraPoints.isEmpty() ? updateClosest() : extraPoints.poll();
        // If we have reached the end, set the flag
        if (res.sameBlock(end)) foundEnd = true;
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
        // Find minimum distance
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            if (distances[i] < minDistance) {
                minDistance = distances[i];
            }
        }

        int[] sub = new int[3];
        boolean needsX = Math.abs(distances[0] - minDistance) <= Vec.EPSILON;
        boolean needsY = Math.abs(distances[1] - minDistance) <= Vec.EPSILON;
        boolean needsZ = Math.abs(distances[2] - minDistance) <= Vec.EPSILON;

        int[] vals = new int[] { steps[0], steps[1], steps[2] };

        System.out.println(Arrays.toString(vals));

        // Update all points that are minimum distance
        if (needsX) {
            if (signums[0] == 1) sub[0] = 1;
            calculateIntersectionX(points[0], direction, signums[0]);
            steps[0] += signums[0];
        }
        if (needsY) {
            if (signums[1] == 1) sub[1] = 1;
            calculateIntersectionY(points[1], direction, signums[1]);
            steps[1] += signums[1];
        }
        if (needsZ) {
            if (signums[2] == 1) sub[2] = 1;
            calculateIntersectionZ(points[2], direction, signums[2]);
            steps[2] += signums[2];
        }

        System.out.println("SIGNUMS " + Arrays.toString(signums));

        // If we pass a grid line in the positive direction, we subtract 1 to get the block we just passed over
        var closest = new Vec(steps[0], steps[1], steps[2]).sub(sub[0], sub[1], sub[2]);

        // If multiple grid lines are cross at the same time, we need to add the blocks that are missed
        if (needsX && needsY && needsZ) {
            extraPoints.add(new Vec(vals[0] + signums[0], vals[1], vals[2]));
            extraPoints.add(new Vec(vals[0], vals[1] + signums[1], vals[1]));
            extraPoints.add(new Vec(vals[0], vals[1], vals[2] + signums[2]));
        } else if (needsX && needsY) {
            extraPoints.add(new Vec(vals[0] + signums[0], vals[1], vals[2]));
            extraPoints.add(new Vec(vals[0], vals[1] + signums[1], vals[1]));
        } else if (needsX && needsZ) {
            extraPoints.add(new Vec(vals[0] + signums[0], vals[1], vals[2]));
            extraPoints.add(new Vec(vals[0], vals[1], vals[2] + signums[2]));
        } else if (needsY && needsZ) {
            extraPoints.add(new Vec(vals[0], vals[1] + signums[1], vals[1]));
            extraPoints.add(new Vec(vals[0], vals[1], vals[2] + signums[2]));
        }

        System.out.println("CLOSEST " + closest);
        System.out.println("ADDED " + extraPoints);

        return closest;
    }
}