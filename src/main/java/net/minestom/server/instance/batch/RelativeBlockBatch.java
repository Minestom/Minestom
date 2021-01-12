package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
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
    // Format: relative pos - blockStateId/customBlockId (16/16 bits)
    private final Long2IntMap blockIdMap = new Long2IntOpenHashMap();

    // Need to be synchronized manually
    // relative pos - data
    private final Long2ObjectMap<Data> blockDataMap = new Long2ObjectOpenHashMap<>();

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
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {

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

        long pos = x;
        pos = (pos << 16) | (short) y;
        pos = (pos << 16) | (short) z;

        final int block = (blockStateId << 16) | customBlockId;
        synchronized (blockIdMap) {
            this.blockIdMap.put(pos, block);

            // Save data if present
            if (data != null) {
                synchronized (blockDataMap) {
                    this.blockDataMap.put(pos, data);
                }
            }
        }
    }

    @Override
    public void clear() {
        synchronized (blockIdMap) {
            this.blockIdMap.clear();
        }
    }

    /**
     * Applies this batch to the given instance at the origin (0, 0, 0) of the instance.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public AbsoluteBlockBatch apply(@NotNull Instance instance, @Nullable Runnable callback) {
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
    public AbsoluteBlockBatch apply(@NotNull Instance instance, @NotNull BlockPosition position, @Nullable Runnable callback) {
        return apply(instance, position.getX(), position.getY(), position.getZ(), callback);
    }

    /**
     * Applies this batch to the given instance at the given position.
     *
     * @param instance The instance in which the batch should be applied
     * @param x The x position to apply the batch
     * @param y The y position to apply the batch
     * @param z The z position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch apply(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        return apply(instance, x, y, z, callback, true);
    }

    /**
     * Applies this batch to the given instance at the given position, and execute the callback
     * immediately when the blocks have been applied, int an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param x The x position to apply the batch
     * @param y The y position to apply the batch
     * @param z The z position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch applyUnsafe(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        return apply(instance, x, y, z, callback, false);
    }

    /**
     * Applies this batch to the given instance at the given position, execute the callback depending on safeCallback.
     *
     * @param instance The instance in which the batch should be applied
     * @param x The x position to apply the batch
     * @param y The y position to apply the batch
     * @param z The z position to apply the batch
     * @param callback The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update. Otherwise it will be executed immediately upon completion
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    protected AbsoluteBlockBatch apply(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback, boolean safeCallback) {
        return this.toAbsoluteBatch(x, y, z).apply(instance, callback, safeCallback);
    }

    /**
     * Converts this batch to an absolute batch at the origin (0, 0, 0).
     *
     * @return An absolute batch of this batch at the origin
     */
    @NotNull
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
    @NotNull
    public AbsoluteBlockBatch toAbsoluteBatch(int x, int y, int z) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch(this.options);
        synchronized (blockIdMap) {
            for (Long2IntMap.Entry entry : blockIdMap.long2IntEntrySet()) {
                final long pos = entry.getLongKey();
                final short relZ = (short) (pos & 0xFFFF);
                final short relY = (short) ((pos >> 16) & 0xFFFF);
                final short relX = (short) ((pos >> 32) & 0xFFFF);

                final int ids = entry.getIntValue();
                final short customBlockId = (short) (ids & 0xFFFF);
                final short blockStateId = (short) ((ids >> 16) & 0xFFFF);

                Data data = null;
                if (!blockDataMap.isEmpty()) {
                    synchronized (blockDataMap) {
                        data = blockDataMap.get(pos);
                    }
                }

                final int finalX = x + offsetX + relX;
                final int finalY = y + offsetY + relY;
                final int finalZ = z + offsetZ + relZ;

                batch.setSeparateBlocks(finalX, finalY, finalZ, blockStateId, customBlockId, data);
            }
        }
        return batch;
    }
}
