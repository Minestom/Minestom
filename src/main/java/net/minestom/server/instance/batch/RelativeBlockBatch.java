package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.*;
import net.minestom.server.data.Data;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RelativeBlockBatch implements Batch<Runnable> {
    // relative pos format: nothing/relative x/relative y/relative z (16/16/16/16 bits)

    // Need to be synchronized manually
    // Format: relative pos - blockStateId/customBlockId (16/16 bits)
    private final Long2IntMap blockIdMap = new Long2IntOpenHashMap();

    // Need to be synchronized manually
    // relative pos - data
    private final Long2ObjectMap<Data> blockDataMap = new Long2ObjectOpenHashMap<>();

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        Check.argCondition(Math.abs(x) > Short.MAX_VALUE, "Relative x position may not be more than 16 bits long.");
        Check.argCondition(Math.abs(y) > Short.MAX_VALUE, "Relative y position may not be more than 16 bits long.");
        Check.argCondition(Math.abs(z) > Short.MAX_VALUE, "Relative z position may not be more than 16 bits long.");

        long pos = x;
        pos = (pos << 16) | (short) y;
        pos = (pos << 16) | (short) z;

        final int block = (blockStateId << 16) | customBlockId;
        synchronized (blockIdMap) {
            this.blockIdMap.put(pos, block);
        }

        if (data != null) {
            synchronized (blockDataMap) {
                this.blockDataMap.put(pos, data);
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
    public void apply(@NotNull InstanceContainer instance, @Nullable Runnable callback) {
        apply(instance, 0, 0, 0, callback);
    }

    public void apply(@NotNull InstanceContainer instance, @NotNull BlockPosition position, @Nullable Runnable callback) {
        apply(instance, position.getX(), position.getY(), position.getZ(), callback);
    }

    public void apply(@NotNull InstanceContainer instance, int x, int y, int z, @Nullable Runnable callback) {
        apply(instance, x, y, z, callback, true);
    }

    public void applyUnsafe(@NotNull InstanceContainer instance, int x, int y, int z, @Nullable Runnable callback) {
        apply(instance, x, y, z, callback, false);
    }

    protected void apply(@NotNull InstanceContainer instance, int x, int y, int z, @Nullable Runnable callback, boolean safeCallback) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch();

        synchronized (blockIdMap) {
            for (Map.Entry<Long, Integer> entry : blockIdMap.long2IntEntrySet()) {
                long pos = entry.getKey();
                short relZ = (short) (pos & 0xFFFF);
                short relY = (short) ((pos >> 16) & 0xFFFF);
                short relX = (short) ((pos >> 32) & 0xFFFF);

                int ids = entry.getValue();
                short customBlockId = (short) (ids & 0xFFFF);
                short blockStateId = (short) ((ids >> 16) & 0xFFFF);

                Data data = null;
                if (!blockDataMap.isEmpty()) {
                    synchronized (blockDataMap) {
                        data = blockDataMap.get(pos);
                    }
                }

                batch.setSeparateBlocks(x + relX, y + relY, z + relZ, blockStateId, customBlockId, data);
            }
        }

        batch.apply(instance, callback, safeCallback);
    }
}
