package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

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

    /**
     * Removes the block at the specified position.
     *
     * @param x The x position of the block to remove
     * @param y The y position of the block to remove
     * @param z The z position of the block to remove
     */
    public void removeBlock(int x, int y, int z) {
        if (firstEntry) return;

        x -= offsetX;
        y -= offsetY;
        z -= offsetZ;

        long pos = Short.toUnsignedLong((short)x);
        pos = (pos << 16) | Short.toUnsignedLong((short)y);
        pos = (pos << 16) | Short.toUnsignedLong((short)z);

        synchronized (blockIdMap) {
            this.blockIdMap.remove(pos);
        }

        if (blockIdMap.isEmpty()) {
            firstEntry = true;
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
    @Contract(pure = true)
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
    @Contract(pure = true)
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
    @Contract(pure = true)
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
    @Contract(pure = true)
    public RelativeBlockBatch rotate(Axis axis, Point origin, int quarterTurns) {
        quarterTurns = ((quarterTurns % 4) + 4) % 4;

        if (quarterTurns == 0) return copy();

        final RelativeBlockBatch rotated = new RelativeBlockBatch(this.options);
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();

        int cos;
        switch (quarterTurns) {
            case 1 -> cos = 0;
            case 2 -> cos = -1;
            case 3 -> cos = 0;
            default -> cos = 1;
        }
        int sin;
        switch (quarterTurns) {
            case 1 -> sin = 1;
            case 2 -> sin = 0;
            case 3 -> sin = -1;
            default -> sin = 0;
        }

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
                        newY = dy * cos - dz * sin;
                        newZ = dy * sin + dz * cos;
                    }
                    case Y -> {
                        newX = dx * cos + dz * sin;
                        newY = dy;
                        newZ = -dx * sin + dz * cos;
                    }
                    case Z -> {
                        newX = dx * cos - dy * sin;
                        newY = dx * sin + dy * cos;
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
    @Contract(pure = true)
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
    @Contract(pure = true)
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
    @Contract(pure = true)
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
    @Contract(pure = true)
    public RelativeBlockBatch rotate(Axis axis, Point origin, double degrees) {
        degrees = ((degrees % 360) + 360) % 360;

        if (degrees == 0) return copy();
        if (degrees % 90 == 0) {
            int quarterTurns = (int) (degrees / 90);
            return rotate(axis, origin, quarterTurns);
        }

        final RelativeBlockBatch rotated = new RelativeBlockBatch(this.options);
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();

        final double cos = Math.cos(Math.toRadians(degrees));
        final double sin = Math.sin(Math.toRadians(degrees));

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
    @Contract(pure = true)
    public RelativeBlockBatch mirrorX(Point origin) {
        return mirror(Axis.X, origin);
    }

    /**
     * Mirrors this batch over the plane perpendicular to the Y axis.
     *
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    @Contract(pure = true)
    public RelativeBlockBatch mirrorY(Point origin) {
        return mirror(Axis.Y, origin);
    }

    /**
     * Mirrors this batch over the plane perpendicular to the Z axis.
     *
     * @param origin The origin point to mirror around
     * @return A new mirrored batch
     */
    @Contract(pure = true)
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
    @Contract(pure = true)
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
     * Merges this batch with another batch, starting with a copy of this batch and then
     * adding blocks from the other batch, allowing the other batch's blocks to overwrite
     * any conflicts.
     *
     * @param other The other batch to merge with
     * @return A new merged batch
     */
    @Contract(pure = true)
    public RelativeBlockBatch mergeWith(RelativeBlockBatch other) {
        return mergeWithOffset(other, 0, 0, 0);
    }

    /**
     * Merges this batch with another batch, starting with a copy of this batch and then
     * adding blocks from the other batch translated by the specified offset vector.
     * Blocks from the other batch overwrite any conflicts.
     *
     * @param other The other batch to merge with
     * @param dx The x offset to apply to the other batch's blocks
     * @param dy The y offset to apply to the other batch's blocks
     * @param dz The z offset to apply to the other batch's blocks
     * @return A new merged batch
     */
    @Contract(pure = true)
    public RelativeBlockBatch mergeWithOffset(RelativeBlockBatch other, int dx, int dy, int dz) {
        final RelativeBlockBatch merged = copy();

        synchronized (other.blockIdMap) {
            for (var entry : other.blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);
                final Block block = entry.getValue();
                final int absX = other.offsetX + relX + dx;
                final int absY = other.offsetY + relY + dy;
                final int absZ = other.offsetZ + relZ + dz;
                merged.setBlock(absX, absY, absZ, block);
            }
        }

        return merged;
    }

    /**
     * Subtracts another batch from this batch, removing blocks at matching positions.
     *
     * @param other The other batch to subtract
     * @return A new batch with blocks removed where the other batch has blocks
     */
    @Contract(pure = true)
    public RelativeBlockBatch subtract(RelativeBlockBatch other) {
        return subtractWithOffset(other, 0, 0, 0, null);
    }

    /**
     * Subtracts another batch from this batch, removing blocks at matching positions and replacing them with a default block.
     *
     * @param other The other batch to subtract
     * @param defaultBlock The block to set in place of removed blocks, or null to remove them
     * @return A new batch with blocks removed or replaced where the other batch has blocks
     */
    @Contract(pure = true)
    public RelativeBlockBatch subtract(RelativeBlockBatch other, @Nullable Block defaultBlock) {
        return subtractWithOffset(other, 0, 0, 0, defaultBlock);
    }

    /**
     * Subtracts another batch from this batch with an offset, removing blocks at matching positions.
     *
     * @param other The other batch to subtract
     * @param dx The x offset to apply to the other batch's positions
     * @param dy The y offset to apply to the other batch's positions
     * @param dz The z offset to apply to the other batch's positions
     * @return A new batch with blocks removed where the offset other batch has blocks
     */
    @Contract(pure = true)
    public RelativeBlockBatch subtractWithOffset(RelativeBlockBatch other, int dx, int dy, int dz) {
        return subtractWithOffset(other, dx, dy, dz, null);
    }

    /**
     * Subtracts another batch from this batch with an offset, removing blocks at matching positions and replacing them with a default block.
     *
     * @param other The other batch to subtract
     * @param dx The x offset to apply to the other batch's positions
     * @param dy The y offset to apply to the other batch's positions
     * @param dz The z offset to apply to the other batch's positions
     * @param defaultBlock The block to set in place of removed blocks, or null to remove them
     * @return A new batch with blocks removed or replaced where the offset other batch has blocks
     */
    @Contract(pure = true)
    public RelativeBlockBatch subtractWithOffset(RelativeBlockBatch other, int dx, int dy, int dz, @Nullable Block defaultBlock) {
        final RelativeBlockBatch subtracted = copy();

        synchronized (other.blockIdMap) {
            for (var entry : other.blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);
                final int absX = other.offsetX + relX + dx;
                final int absY = other.offsetY + relY + dy;
                final int absZ = other.offsetZ + relZ + dz;
                if (defaultBlock == null) subtracted.removeBlock(absX, absY, absZ);
                else subtracted.setBlock(absX, absY, absZ, defaultBlock);
            }
        }
        return subtracted;
    }

    /**
     * Translates this batch by the specified offsets.
     *
     * @param dx The x offset to apply
     * @param dy The y offset to apply
     * @param dz The z offset to apply
     * @return A new translated batch
     */
    @Contract(pure = true)
    public RelativeBlockBatch translate(int dx, int dy, int dz) {
        final RelativeBlockBatch translated = new RelativeBlockBatch(this.options);
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);
                final Block block = entry.getValue();
                translated.setBlock(offsetX + relX + dx, offsetY + relY + dy, offsetZ + relZ + dz, block);
            }
        }
        return translated;
    }

    /**
     * Transforms this batch by applying a function to each block.
     *
     * @param transformer The function to apply to each block
     * @return A new transformed batch
     */
    @Contract(pure = true)
    public RelativeBlockBatch transform(Function<Block, Block> transformer) {
        final RelativeBlockBatch transformed = new RelativeBlockBatch(this.options);
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);
                final Block block = entry.getValue();
                transformed.setBlock(offsetX + relX, offsetY + relY, offsetZ + relZ, transformer.apply(block));
            }
        }
        return transformed;
    }


    /**
     * Creates a copy of this batch.
     *
     * @return A new batch with the same blocks
     */
    @Contract(pure = true)
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
     * Returns the number of blocks in this batch.
     *
     * @return The block count
     */
    @Contract(pure = true)
    public int getBlockCount() {
        return blockIdMap.size();
    }

    /**
     * Returns the bounding box of this batch.
     *
     * @return The bounding box
     */
    @Contract(pure = true)
    public BoundingBox getBounds() {
        if (blockIdMap.isEmpty()) {
            return BoundingBox.ZERO;
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);
                final int absX = offsetX + relX;
                final int absY = offsetY + relY;
                final int absZ = offsetZ + relZ;
                minX = Math.min(minX, absX);
                minY = Math.min(minY, absY);
                minZ = Math.min(minZ, absZ);
                maxX = Math.max(maxX, absX);
                maxY = Math.max(maxY, absY);
                maxZ = Math.max(maxZ, absZ);
            }
        }
        return new BoundingBox(new Vec(minX, minY, minZ), new Vec(maxX + 1, maxY + 1, maxZ + 1));
    }

    /**
     * Gets the set of chunk indices that will be affected by applying this batch at the origin (0, 0, 0) of the instance.
     * <p>
     * Each chunk index is a {@code long} value representing the unique identifier of a chunk,
     * computed using {@link CoordConversion#chunkIndex(int, int)}.
     *
     * @return A set of chunk indices affected by this batch
     */
    @Override
    @Contract(pure = true)
    public Set<Long> getAffectedChunks() {
        return getAffectedChunks(0, 0);
    }

    /**
     * Gets the set of chunk indices that will be affected by applying this batch.
     * <p>
     * Each chunk index is a {@code long} value representing the unique identifier of a chunk,
     * computed using {@link CoordConversion#chunkIndex(int, int)}.
     *
     * @param x The x position of the origin of the batch
     * @param z The z position of the origin of the batch
     * @return A set of chunk indices affected by this batch
     */
    @Contract(pure = true)
    public Set<Long> getAffectedChunks(int x, int z) {
        Set<Long> affectedChunks = new HashSet<>();
        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final int finalX = x + offsetX + relX;
                final int finalZ = z + offsetZ + relZ;

                final int chunkX = CoordConversion.globalToChunk(finalX);
                final int chunkZ = CoordConversion.globalToChunk(finalZ);
                final long chunkIndex = CoordConversion.chunkIndex(chunkX, chunkZ);
                affectedChunks.add(chunkIndex);
            }
        }
        return affectedChunks;
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
    @Contract(pure = true)
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
    @Contract(pure = true)
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
