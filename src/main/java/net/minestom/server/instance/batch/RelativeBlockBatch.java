package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

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
public class RelativeBlockBatch implements Batch {
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
     * Applies this batch to the given instance at the origin (0, 0, 0) of the instance.
     *
     * @param instance The instance in which the batch should be applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance) {
        return apply(instance, 0, 0, 0);
    }

    /**
     * Applies this batch to the given instance at the given block position.
     *
     * @param instance The instance in which the batch should be applied
     * @param position The position to apply the batch
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance, @NotNull Point position) {
        return apply(instance, position.blockX(), position.blockY(), position.blockZ());
    }

    /**
     * Applies this batch to the given instance at the given position, execute the callback depending on safeCallback.
     *
     * @param instance     The instance in which the batch should be applied
     * @param x            The x position to apply the batch
     * @param y            The y position to apply the batch
     * @param z            The z position to apply the batch
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance, int x, int y, int z) {
        return this.toAbsoluteBatch(x, y, z).apply(instance);
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
}
