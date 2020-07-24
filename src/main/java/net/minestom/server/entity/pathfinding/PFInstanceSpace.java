package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

public class PFInstanceSpace implements IInstanceSpace {

    private Instance instance;
    private Map<Chunk, PFColumnarSpace> chunkSpaceMap = new HashMap<>();

    public PFInstanceSpace(Instance instance) {
        this.instance = instance;
    }

    @Override
    public IBlockObject blockObjectAt(int x, int y, int z) {
        final short blockId = instance.getBlockId(x, y, z);
        final Block block = Block.fromId(blockId);
        return new PFBlockObject(block);
    }

    @Override
    public IColumnarSpace columnarSpaceAt(int cx, int cz) {
        final Chunk chunk = instance.getChunk(cx, cz);
        final PFColumnarSpace columnarSpace =
                chunkSpaceMap.computeIfAbsent(chunk, c -> {
                    final PFColumnarSpace cs = new PFColumnarSpace(this, c);
                    c.setColumnarSpace(cs);
                    return cs;
                });

        return columnarSpace;
    }

    public Instance getInstance() {
        return instance;
    }
}
