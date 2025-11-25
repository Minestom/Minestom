package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Batch} which can be used when changes are required across chunk borders, and
 * are going to be reused in different places. If translation is not required, {@link AbsoluteBlockBatch}
 * should be used instead for efficiency purposes.
 * <p>
 * Coordinates are relative to (0, 0, 0) with some limitations. All coordinates must
 * fit within a 16 bit integer of the first coordinate (32,767 blocks). If blocks must
 * be spread out over a larger area, an {@link AbsoluteBlockBatch} should be used.
 * <p>
 * All inverses are {@link AbsoluteBlockBatch}s and represent the inverse of the application
 * at the position which it was applied.
 * <p>
 * If a batch will be used multiple times at the same coordinate, it is suggested
 * to convert it to an {@link AbsoluteBlockBatch} and cache the result. Application
 * of absolute batches (currently) is significantly faster than their relative counterpart.
 *
 * @see Batch
 * @see AbsoluteBlockBatch
 */
public class RelativeBlockBatch implements Batch<Runnable> {
    // relative pos format: nothing/relative x/relative y/relative z (16/16/16/16 bits)

    // Need to be synchronized manually
    // Format: relative pos - block
    private final Long2ObjectMap<Block> blockIdMap = new Long2ObjectOpenHashMap<>();

    private final BatchOption options;

    private volatile boolean firstEntry = true;
    private int offsetX, offsetY, offsetZ;

    public RelativeBlockBatch() {
        this(new BatchOption());
    }

    public RelativeBlockBatch(BatchOption options) {
        this.options = options;
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        // Save the offsets if it is the first entry
        if (firstEntry) {
            this.firstEntry = false;

            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
        }

        // Subtract offset
        x -= offsetX;
        y -= offsetY;
        z -= offsetZ;

        // Verify that blocks are not too far from each other
        Check.argCondition(Math.abs(x) > Short.MAX_VALUE, "Relative x position may not be more than 16 bits long.");
        Check.argCondition(Math.abs(y) > Short.MAX_VALUE, "Relative y position may not be more than 16 bits long.");
        Check.argCondition(Math.abs(z) > Short.MAX_VALUE, "Relative z position may not be more than 16 bits long.");

        long pos = Short.toUnsignedLong((short)x);
        pos = (pos << 16) | Short.toUnsignedLong((short)y);
        pos = (pos << 16) | Short.toUnsignedLong((short)z);

        //final int block = (blockStateId << 16) | customBlockId;
        synchronized (blockIdMap) {
            this.blockIdMap.put(pos, block);
        }
    }

    @Override
    public void clear() {
        synchronized (blockIdMap) {
            this.blockIdMap.clear();
        }
    }

    /**
     * Rotates this batch around the X axis.
     *
     * @param origin The origin point to rotate around
     * @param quarterTurns The number of 90-degree clockwise turns
     * @return A new rotated batch
     */
    public RelativeBlockBatch rotateX(Point origin, int quarterTurns) {
        return rotate(Axis.X, origin, quarterTurns);
    }

    /**
     * Rotates this batch around the Y axis.
     *
     * @param origin The origin point to rotate around
     * @param quarterTurns The number of 90-degree clockwise turns
     * @return A new rotated batch
     */
    public RelativeBlockBatch rotateY(Point origin, int quarterTurns) {
        return rotate(Axis.Y, origin, quarterTurns);
    }

    /**
     * Rotates this batch around the Z axis.
     *
     * @param origin The origin point to rotate around
     * @param quarterTurns The number of 90-degree clockwise turns
     * @return A new rotated batch
     */
    public RelativeBlockBatch rotateZ(Point origin, int quarterTurns) {
        return rotate(Axis.Z, origin, quarterTurns);
    }

    /**
     * Rotates this batch around the specified axis.
     *
     * @param axis The axis to rotate around
     * @param origin The origin point to rotate around
     * @param quarterTurns The number of 90-degree clockwise turns
     * @return A new rotated batch
     */
    public RelativeBlockBatch rotate(Axis axis, Point origin, int quarterTurns) {
        return rotate(axis, origin, quarterTurns * 90D);
    }

    /**
     * Rotates this batch around the X axis.
     * <p>
     * <strong>Warning:</strong> This method uses floating-point arithmetic and rounds coordinates to integers,
     * which may result in data loss or imprecise positioning for non-90-degree rotations.
     *
     * @param origin The origin point to rotate around
     * @param degrees The number of degrees to rotate clockwise
     * @return A new rotated batch
     */
    @ApiStatus.Experimental
    public RelativeBlockBatch rotateX(Point origin, double degrees) {
        return rotate(Axis.X, origin, degrees);
    }

    /**
     * Rotates this batch around the Y axis.
     * <p>
     * <strong>Warning:</strong> This method uses floating-point arithmetic and rounds coordinates to integers,
     * which may result in data loss or imprecise positioning for non-90-degree rotations.
     *
     * @param origin The origin point to rotate around
     * @param degrees The number of degrees to rotate clockwise
     * @return A new rotated batch
     */
    @ApiStatus.Experimental
    public RelativeBlockBatch rotateY(Point origin, double degrees) {
        return rotate(Axis.Y, origin, degrees);
    }

    /**
     * Rotates this batch around the Z axis.
     * <p>
     * <strong>Warning:</strong> This method uses floating-point arithmetic and rounds coordinates to integers,
     * which may result in data loss or imprecise positioning for non-90-degree rotations.
     *
     * @param origin The origin point to rotate around
     * @param degrees The number of degrees to rotate clockwise
     * @return A new rotated batch
     */
    @ApiStatus.Experimental
    public RelativeBlockBatch rotateZ(Point origin, double degrees) {
        return rotate(Axis.Z, origin, degrees);
    }

    /**
     * Rotates this batch around the specified axis using degrees.
     * <p>
     * <strong>Warning:</strong> This method uses floating-point arithmetic and rounds coordinates to integers,
     * which may result in data loss or imprecise positioning for non-90-degree rotations.
     *
     * @param axis The axis to rotate around
     * @param origin The origin point to rotate around
     * @param degrees The number of degrees to rotate clockwise
     * @return A new rotated batch
     */
    @ApiStatus.Experimental
    public RelativeBlockBatch rotate(Axis axis, Point origin, double degrees) {
        degrees = ((degrees % 360) + 360) % 360;

        if (degrees == 0) return copy();

        final RelativeBlockBatch rotated = new RelativeBlockBatch(this.options);
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();

        final double cos = Math.cos(degrees);
        final double sin = Math.sin(degrees);

        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final Block block = entry.getValue();

                final int absX = offsetX + relX;
                final int absY = offsetY + relY;
                final int absZ = offsetZ + relZ;

                final int dx = absX - originX;
                final int dy = absY - originY;
                final int dz = absZ - originZ;

                final int newX, newY, newZ;
                switch (axis) {
                    case X -> {
                        newX = dx;
                        newY = Math.toIntExact(Math.round(dy * cos - dz * sin));
                        newZ = Math.toIntExact(Math.round(dy * sin + dz * cos));
                    }
                    case Y -> {
                        newX = Math.toIntExact(Math.round(dx * cos + dz * sin));
                        newY = dy;
                        newZ = Math.toIntExact(Math.round(-dx * sin + dz * cos));
                    }
                    case Z -> {
                        newX = Math.toIntExact(Math.round(dx * cos - dy * sin));
                        newY = Math.toIntExact(Math.round(dx * sin + dy * cos));
                        newZ = dz;
                    }
                    default -> throw new IllegalArgumentException("Invalid axis: " + axis);
                }

                rotated.setBlock(newX + originX, newY + originY, newZ + originZ, block);
            }
        }

        return rotated;
    }

    /**
     * Mirrors this batch over the plane perpendicular to the X axis.
     *
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    public RelativeBlockBatch mirrorX(Point origin) {
        return mirror(Axis.X, origin);
    }

    /**
     * Mirrors this batch over the plane perpendicular to the Y axis.
     *
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    public RelativeBlockBatch mirrorY(Point origin) {
        return mirror(Axis.Y, origin);
    }

    /**
     * Mirrors this batch over the plane perpendicular to the Z axis.
     *
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    public RelativeBlockBatch mirrorZ(Point origin) {
        return mirror(Axis.Z, origin);
    }

    /**
     * Mirrors this batch over the plane perpendicular to the specified axis.
     *
     * @param axis The axis perpendicular to the mirror plane
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    public RelativeBlockBatch mirror(Axis axis, Point origin) {
        final RelativeBlockBatch mirrored = new RelativeBlockBatch(this.options);
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();

        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final Block block = entry.getValue();

                final int absX = offsetX + relX;
                final int absY = offsetY + relY;
                final int absZ = offsetZ + relZ;

                final int newX, newY, newZ;
                switch (axis) {
                    case X -> {
                        newX = 2 * originX - absX;
                        newY = absY;
                        newZ = absZ;
                    }
                    case Y -> {
                        newX = absX;
                        newY = 2 * originY - absY;
                        newZ = absZ;
                    }
                    case Z -> {
                        newX = absX;
                        newY = absY;
                        newZ = 2 * originZ - absZ;
                    }
                    default -> throw new IllegalArgumentException("Invalid axis: " + axis);
                }

                mirrored.setBlock(newX, newY, newZ, block);
            }
        }

        return mirrored;
    }

    /**
     * Creates a copy of this batch.
     *
     * @return A new batch with the same blocks
     */
    public RelativeBlockBatch copy() {
        final RelativeBlockBatch copy = new RelativeBlockBatch(this.options);
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final Block block = entry.getValue();
                copy.setBlock(offsetX + relX, offsetY + relY, offsetZ + relZ, block);
            }
        }
        return copy;
    }

    /**
     * Applies this batch to the given instance at the origin (0, 0, 0) of the instance.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public AbsoluteBlockBatch apply(Instance instance, @Nullable Runnable callback) {
        return apply(instance, 0, 0, 0, callback);
    }

    /**
     * Applies this batch to the given instance at the given block position.
     *
     * @param instance The instance in which the batch should be applied
     * @param position The position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch apply(Instance instance, Point position, @Nullable Runnable callback) {
        return apply(instance, position.blockX(), position.blockY(), position.blockZ(), callback);
    }

    /**
     * Applies this batch to the given instance at the given position.
     *
     * @param instance The instance in which the batch should be applied
     * @param x        The x position to apply the batch
     * @param y        The y position to apply the batch
     * @param z        The z position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch apply(Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        return apply(instance, x, y, z, callback, true);
    }

    /**
     * Applies this batch to the given instance at the given position, and execute the callback
     * immediately when the blocks have been applied, int an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param x        The x position to apply the batch
     * @param y        The y position to apply the batch
     * @param z        The z position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch applyUnsafe(Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        return apply(instance, x, y, z, callback, false);
    }

    /**
     * Applies this batch to the given instance at the given position, execute the callback depending on safeCallback.
     *
     * @param instance     The instance in which the batch should be applied
     * @param x            The x position to apply the batch
     * @param y            The y position to apply the batch
     * @param z            The z position to apply the batch
     * @param callback     The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update. Otherwise it will be executed immediately upon completion
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    protected AbsoluteBlockBatch apply(Instance instance, int x, int y, int z, @Nullable Runnable callback, boolean safeCallback) {
        return this.toAbsoluteBatch(x, y, z).apply(instance, callback, safeCallback);
    }

    /**
     * Converts this batch to an absolute batch at the origin (0, 0, 0).
     *
     * @return An absolute batch of this batch at the origin
     */
    public AbsoluteBlockBatch toAbsoluteBatch() {
        return toAbsoluteBatch(0, 0, 0);
    }

    /**
     * Converts this batch to an absolute batch at the given coordinates.
     *
     * @param x The x position of the batch in the world
     * @param y The y position of the batch in the world
     * @param z The z position of the batch in the world
     * @return An absolute batch of this batch at (x, y, z)
     */
    public AbsoluteBlockBatch toAbsoluteBatch(int x, int y, int z) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch(this.options);
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final Block block = entry.getValue();

                final int finalX = x + offsetX + relX;
                final int finalY = y + offsetY + relY;
                final int finalZ = z + offsetZ + relZ;

                batch.setBlock(finalX, finalY, finalZ, block);
            }
        }
        return batch;
    }

    public enum Axis {
        X, Y, Z
    }
}
