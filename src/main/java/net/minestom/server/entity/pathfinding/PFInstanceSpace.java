package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PFInstanceSpace implements IInstanceSpace {
    private final Instance instance;
    private final Map<Long, IColumnarSpace> chunkSpaceMap = new ConcurrentHashMap<>();

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
        long chunkIndex = ChunkUtils.getChunkIndex(cx, cz);
        PFInstanceSpace space = this;
        return chunkSpaceMap.computeIfAbsent(chunkIndex, c -> instance.createColumnarSpace(space, cx, cz));
    }

    public Instance getInstance() {
        return instance;
    }
}
