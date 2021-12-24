package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PFInstanceSpace implements IInstanceSpace {
    private final Instance instance;
    private final Map<Chunk, PFColumnarSpace> chunkSpaceMap = new ConcurrentHashMap<>();

    public PFInstanceSpace(Instance instance) {
        this.instance = instance;
    }

    @Override
    public IBlockObject blockObjectAt(int x, int y, int z) {
        final Block block = instance.getBlock(x, y, z);
        return PFBlock.get(block);
    }

    @Override
    public IColumnarSpace columnarSpaceAt(int cx, int cz) {
        final Chunk chunk = instance.getChunk(cx, cz);
        if (chunk == null) return null;
        return chunkSpaceMap.computeIfAbsent(chunk, c -> {
            final PFColumnarSpace cs = new PFColumnarSpace(this, c);
            c.setColumnarSpace(cs);
            return cs;
        });
    }

    public Instance getInstance() {
        return instance;
    }
}
