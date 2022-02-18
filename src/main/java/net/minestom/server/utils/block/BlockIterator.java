package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Point> {

    private final int maxDistance;

    private static final int gridSize = 1 << 24;

    private boolean end = false;

    private Point[] blockQueue = new Point[3];
    private int currentBlock = 0;
    private int currentDistance = 0;
    private int maxDistanceInt;

    private int secondError;
    private int thirdError;

    private int secondStep;
    private int thirdStep;

    private BlockFace mainFace;
    private BlockFace secondFace;
    private BlockFace thirdFace;

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
    public BlockIterator(@NotNull Vec start, @NotNull Vec direction, double yOffset, int maxDistance) {
        this.maxDistance = maxDistance;


        Vec startClone = start.withY(y -> y+yOffset);

        currentDistance = 0;

        double mainDirection = 0;
        double secondDirection = 0;
        double thirdDirection = 0;

        double mainPosition = 0;
        double secondPosition = 0;
        double thirdPosition = 0;

        Vec startBlock = startClone.apply(Vec.Operator.FLOOR);

        if (getXLength(direction) > mainDirection) {
            mainFace = getXFace(direction);
            mainDirection = getXLength(direction);
            mainPosition = getXPosition(direction, startClone, startBlock);

            secondFace = getYFace(direction);
            secondDirection = getYLength(direction);
            secondPosition = getYPosition(direction, startClone, startBlock);

            thirdFace = getZFace(direction);
            thirdDirection = getZLength(direction);
            thirdPosition = getZPosition(direction, startClone, startBlock);
        }
        if (getYLength(direction) > mainDirection) {
            mainFace = getYFace(direction);
            mainDirection = getYLength(direction);
            mainPosition = getYPosition(direction, startClone, startBlock);

            secondFace = getZFace(direction);
            secondDirection = getZLength(direction);
            secondPosition = getZPosition(direction, startClone, startBlock);

            thirdFace = getXFace(direction);
            thirdDirection = getXLength(direction);
            thirdPosition = getXPosition(direction, startClone, startBlock);
        }
        if (getZLength(direction) > mainDirection) {
            mainFace = getZFace(direction);
            mainDirection = getZLength(direction);
            mainPosition = getZPosition(direction, startClone, startBlock);

            secondFace = getXFace(direction);
            secondDirection = getXLength(direction);
            secondPosition = getXPosition(direction, startClone, startBlock);

            thirdFace = getYFace(direction);
            thirdDirection = getYLength(direction);
            thirdPosition = getYPosition(direction, startClone, startBlock);
        }

        // trace line backwards to find intercept with plane perpendicular to the main axis

        double d = mainPosition / mainDirection; // how far to hit face behind
        double second = secondPosition - secondDirection * d;
        double third = thirdPosition - thirdDirection * d;

        // Guarantee that the ray will pass though the start block.
        // It is possible that it would miss due to rounding
        // This should only move the ray by 1 grid position
        secondError = floor(second * gridSize);
        secondStep = round(secondDirection / mainDirection * gridSize);
        thirdError = floor(third * gridSize);
        thirdStep = round(thirdDirection / mainDirection * gridSize);

        if (secondError + secondStep <= 0) {
            secondError = -secondStep + 1;
        }

        if (thirdError + thirdStep <= 0) {
            thirdError = -thirdStep + 1;
        }

        Vec lastBlock;

        lastBlock = startBlock.relative(mainFace.getOppositeFace());

        if (secondError < 0) {
            secondError += gridSize;
            lastBlock = lastBlock.relative(secondFace.getOppositeFace());
        }

        if (thirdError < 0) {
            thirdError += gridSize;
            lastBlock = lastBlock.relative(thirdFace.getOppositeFace());
        }

        // This means that when the variables are positive, it means that the coord=1 boundary has been crossed
        secondError -= gridSize;
        thirdError -= gridSize;

        blockQueue[0] = lastBlock;
        currentBlock = -1;

        scan();

        boolean startBlockFound = false;

        for (int cnt = currentBlock; cnt >= 0; cnt--) {
            if (blockEquals(blockQueue[cnt], startBlock)) {
                currentBlock = cnt;
                startBlockFound = true;
                break;
            }
        }

        if (!startBlockFound) {
            throw new IllegalStateException("Start block missed in BlockIterator");
        }

        // Calculate the number of planes passed to give max distance
        maxDistanceInt = round(maxDistance / (Math.sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection));

    }

    private boolean blockEquals(@NotNull Point a, @NotNull Point b) {
        return a.x() == b.x() && a.y() == b.y() && a.z() == b.z();
    }

    private BlockFace getXFace(@NotNull Point direction) {
        return ((direction.x() > 0) ? BlockFace.EAST : BlockFace.WEST);
    }

    private BlockFace getYFace(@NotNull Point direction) {
        return ((direction.y() > 0) ? BlockFace.TOP : BlockFace.BOTTOM);
    }

    private BlockFace getZFace(@NotNull Point direction) {
        return ((direction.z() > 0) ? BlockFace.SOUTH : BlockFace.NORTH);
    }

    private double getXLength(@NotNull Point direction) {
        return Math.abs(direction.x());
    }

    private double getYLength(@NotNull Point direction) {
        return Math.abs(direction.y());
    }

    private double getZLength(@NotNull Point direction) {
        return Math.abs(direction.z());
    }

    private double getPosition(double direction, double position, int blockPosition) {
        return direction > 0 ? (position - blockPosition) : (blockPosition + 1 - position);
    }

    private double getXPosition(@NotNull Point direction, @NotNull Point position, @NotNull Point block) {
        return getPosition(direction.x(), position.x(), block.blockX());
    }

    private double getYPosition(@NotNull Point direction, @NotNull Point position, @NotNull Point block) {
        return getPosition(direction.y(), position.y(), block.blockY());
    }

    private double getZPosition(@NotNull Point direction, @NotNull Point position, @NotNull Point block) {
        return getPosition(direction.z(), position.z(), block.blockZ());
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
        scan();
        return currentBlock != -1;
    }

    /**
     * Returns the next BlockPosition in the trace
     *
     * @return the next BlockPosition in the trace
     */
    @Override
    @NotNull
    public Point next() throws NoSuchElementException {
        scan();
        if (currentBlock <= -1) {
            throw new NoSuchElementException();
        } else {
            return blockQueue[currentBlock--];
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("[BlockIterator] doesn't support block removal");
    }

    private void scan() {
        if (currentBlock >= 0) {
            return;
        }
        if (maxDistance != 0 && currentDistance > maxDistanceInt) {
            end = true;
            return;
        }
        if (end) {
            return;
        }

        currentDistance++;

        secondError += secondStep;
        thirdError += thirdStep;

        if (secondError > 0 && thirdError > 0) {
            blockQueue[2] = blockQueue[0].relative(mainFace);
            if (((long) secondStep) * ((long) thirdError) < ((long) thirdStep) * ((long) secondError)) {
                blockQueue[1] = blockQueue[2].relative(secondFace);
                blockQueue[0] = blockQueue[1].relative(thirdFace);
            } else {
                blockQueue[1] = blockQueue[2].relative(thirdFace);
                blockQueue[0] = blockQueue[1].relative(secondFace);
            }
            thirdError -= gridSize;
            secondError -= gridSize;
            currentBlock = 2;
        } else if (secondError > 0) {
            blockQueue[1] = blockQueue[0].relative(mainFace);
            blockQueue[0] = blockQueue[1].relative(secondFace);
            secondError -= gridSize;
            currentBlock = 1;
        } else if (thirdError > 0) {
            blockQueue[1] = blockQueue[0].relative(mainFace);
            blockQueue[0] = blockQueue[1].relative(thirdFace);
            thirdError -= gridSize;
            currentBlock = 1;
        } else {
            blockQueue[0] = blockQueue[0].relative(mainFace);
            currentBlock = 0;
        }
    }

    public static int floor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int round(double num) {
        return floor(num + 0.5d);
    }
}
