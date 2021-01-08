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

public class RelativeBlockBatch implements Batch<Runnable> {
    // relative pos format: nothing/relative x/relative y/relative z (16/16/16/16 bits)

    // Need to be synchronized manually
    // Format: relative pos - blockStateId/customBlockId (16/16 bits)
    private final Long2IntMap blockIdMap = new Long2IntOpenHashMap();

    // Need to be synchronized manually
    // relative pos - data
    private final Long2ObjectMap<Data> blockDataMap = new Long2ObjectOpenHashMap<>();

    private volatile boolean firstEntry = true;
    private int offsetX, offsetY, offsetZ;

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {

        // Save the offsets if it is the first entry
        if (!firstEntry) {
            this.firstEntry = true;

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

    @Override
    public void apply(@NotNull Instance instance, @Nullable Runnable callback) {
        apply(instance, 0, 0, 0, callback);
    }

    public void apply(@NotNull Instance instance, @NotNull BlockPosition position, @Nullable Runnable callback) {
        apply(instance, position.getX(), position.getY(), position.getZ(), callback);
    }

    public void apply(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        apply(instance, x, y, z, callback, true);
    }

    public void applyUnsafe(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback) {
        apply(instance, x, y, z, callback, false);
    }

    protected void apply(@NotNull Instance instance, int x, int y, int z, @Nullable Runnable callback, boolean safeCallback) {
        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();

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

        batch.apply(instance, callback, safeCallback);
    }
}
