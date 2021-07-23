package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Batch} which can be used when changes are required across chunk borders, and
 * are going to be reused in different places. If translation is not required, {@link AbsoluteBlockBatch}
 * should be used instead for efficiency purposes.
 * <p>
 * Coordinates are relative to (0, 0, 0) with some limitations. All coordinates must
 * fit within a 20+1 bit integer (where the +1 is the sign) of the first coordinate (this area
 * covers a range of 1,048,575 blocks in each direction). If blocks must
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
    public void setBlock(int x, int y, int z, @NotNull Block block) {
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

        // (2^20)-1 equals 1,048,575
        
        // Verify that blocks are not too far from each other
        Check.argCondition(x < -1_048_576 || x > 1_048_575, "Relative x position may not be more than 20 bits long.");
        Check.argCondition(y < -1_048_576 || y > 1_048_575, "Relative y position may not be more than 20 bits long.");
        Check.argCondition(z < -1_048_576 || z > 1_048_575, "Relative z position may not be more than 20 bits long.");

        //Bits are used effectively to store as much data as possible:
        //
        //    Sign of Z
        //    |
        //    |Sign of Y
        //    ||
        //    ||Sign of X                                          Bits 0-19: Z
        //    |||                           Bits 20-39: Y     /--------------------\
        //    |||     Bits 40-59: X     /--------------------\|                    |
        //    |||/--------------------\ |                    ||                    |
        //    ||||                    | |                    ||                    |
        //    ||||                    | |                    ||                    |
        //    ||||                    | |                    ||                    |
        //   00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
        
        // We could get away with using less data for Y, but it is future proof for 1.18+ worlds
        // (though it is very much unlikely, that 2^20 vertical blocks will be affected by a single batch)
        
        //Add the coordinates to the long
        long pos = Math.abs(x) & ((1L << 20)-1);
        pos = pos << 20 | ((long)Math.abs(y) & ((1L << 20)-1));
        pos = pos << 20 | ((long)Math.abs(z) & ((1L << 20)-1));

        //Add the coordinate signs to the long
        pos = pos | (x < 0 ? 1L << 60 : 0);
        pos = pos | (y < 0 ? 1L << 61 : 0);
        pos = pos | (z < 0 ? 1L << 62 : 0);

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
    public AbsoluteBlockBatch apply(@NotNull Instance instance, @NotNull Point position, @Nullable Runnable callback) {
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
    public AbsoluteBlockBatch apply(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
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
    public AbsoluteBlockBatch applyUnsafe(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
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
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();
                //For details on the bits used, see #setBlock(int, int, int, Block)
                
                //Get relative unsigned coordinates from the data
                long relXUnsigned = (pos >> 40) & 1_048_575;
                long relYUnsigned = (pos >> 20) & 1_048_575;
                long relZUnsigned =  pos        & 1_048_575;

                //Get and apply the stored sign
                int relX = (int) (relXUnsigned * (((pos >> 60) & 1) == 1 ? -1 : 1));
                int relY = (int) (relYUnsigned * (((pos >> 61) & 1) == 1 ? -1 : 1));
                int relZ = (int) (relZUnsigned * (((pos >> 62) & 1) == 1 ? -1 : 1));

                final Block block = entry.getValue();

                final int finalX = x + offsetX + relX;
                final int finalY = y + offsetY + relY;
                final int finalZ = z + offsetZ + relZ;

                batch.setBlock(finalX, finalY, finalZ, block);
            }
        }
        return batch;
    }
}
