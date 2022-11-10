package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {
    private final short[] signums = new short[3];
    private final Vec end;
    private final boolean smooth;

    private boolean foundEnd = false;

    //length of ray from current position to next x or y-side
    double sideDistX;
    double sideDistY;
    double sideDistZ;

    //length of ray from one x or y-side to next x or y-side
    private final double deltaDistX;
    private final double deltaDistY;
    private final double deltaDistZ;

    //which box of the map we're in
    int mapX;
    int mapY;
    int mapZ;

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
     * @param smooth      A boolean indicating whether the cast should be smooth.
     *                    Smooth casts will only include one block when intersecting multiple axis lines.
     * @param maxDistance This is the maximum distance in blocks for the
     *                    trace. Setting this value above 140 may lead to problems with
     *                    unloaded chunks. A value of 0 indicates no limit
     */
    public BlockIterator(@NotNull Vec start, @NotNull Vec direction, double yOffset, double maxDistance, boolean smooth) {
        start = start.add(0, yOffset, 0);
        end = start.add(direction.normalize().mul(maxDistance));
        if (direction.isZero()) this.foundEnd = true;

        this.smooth = smooth;

        Vec ray = direction.normalize();

        //which box of the map we're in
        mapX = start.blockX();
        mapY = start.blockY();
        mapZ = start.blockZ();

        signums[0] = (short) Math.signum(direction.x());
        signums[1] = (short) Math.signum(direction.y());
        signums[2] = (short) Math.signum(direction.z());

        deltaDistX = (ray.x() == 0) ? 1e30 : Math.abs(1 / ray.x());
        deltaDistY = (ray.y() == 0) ? 1e30 : Math.abs(1 / ray.y());        // Find grid intersections for x, y, z
        deltaDistZ = (ray.z() == 0) ? 1e30 : Math.abs(1 / ray.z());        // This works by calculating and storing the distance to the next grid intersection on the x, y and z axis

        //calculate step and initial sideDist
        if (ray.x() < 0) {
            sideDistX = (start.x() - mapX) * deltaDistX;
        } else {
            sideDistX = (mapX + 1.0 - start.x()) * deltaDistX;
        }
        if (ray.y() < 0) {
            sideDistY = (start.y() - mapY) * deltaDistY;
        } else {
            sideDistY = (mapY + 1.0 - start.y()) * deltaDistY;
        }
        if (ray.z() < 0) {
            sideDistZ = (start.z() - mapZ) * deltaDistZ;
        } else {
            sideDistZ = (mapZ + 1.0 - start.z()) * deltaDistZ;
        }
    }

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
        this(start, direction, yOffset, maxDistance, false);
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
        this(pos.asVec(), pos.direction(), yOffset, maxDistance, false);
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
        this(pos.asVec(), pos.direction(), yOffset, 0, false);
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
        if (!extraPoints.isEmpty()) {
            var res = extraPoints.poll();
            if (res.sameBlock(end)) foundEnd = true;
            return res;
        }

        var current = new Vec(mapX, mapY, mapZ);
        if (current.sameBlock(end)) foundEnd = true;

        double closest = Math.min(sideDistX, Math.min(sideDistY, sideDistZ));
        boolean needsX = sideDistX - closest < 1e-10;
        boolean needsY = sideDistY - closest < 1e-10;
        boolean needsZ = sideDistZ - closest < 1e-10;

        if (needsZ) {
            sideDistZ += deltaDistZ;
            mapZ += signums[2];
        }

        if (needsX) {
            sideDistX += deltaDistX;
            mapX += signums[0];
        }

        if (needsY) {
            sideDistY += deltaDistY;
            mapY += signums[1];
        }

        if (needsX && needsY && needsZ) {
            extraPoints.add(new Vec(signums[0] + current.x(), current.y(), current.z()));
            if (smooth) return current;
            extraPoints.add(new Vec(current.x(), signums[1] + current.y(), current.z()));
            extraPoints.add(new Vec(current.x(), current.y(), signums[2] + current.z()));
        } else if (needsX && needsY) {
            extraPoints.add(new Vec(signums[0] + current.x(), current.y(), current.z()));
            if (smooth) return current;
            extraPoints.add(new Vec(current.x(), signums[1] + current.y(), current.z()));
        } else if (needsX && needsZ) {
            extraPoints.add(new Vec(signums[0] + current.x(), current.y(), current.z()));
            if (smooth) return current;
            extraPoints.add(new Vec(current.x(), current.y(), signums[2] + current.z()));
        } else if (needsY && needsZ) {
            extraPoints.add(new Vec(current.x(), signums[1] + current.y(), current.z()));
            if (smooth) return current;
            extraPoints.add(new Vec(current.x(), current.y(), signums[2] + current.z()));
        }

        return current;
    }
}