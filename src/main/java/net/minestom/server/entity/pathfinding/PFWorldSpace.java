package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.world.Chunk;
import net.minestom.server.world.World;
import net.minestom.server.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PFWorldSpace implements IInstanceSpace {

    private final World world;
    private final Map<Chunk, PFColumnarSpace> chunkSpaceMap = new ConcurrentHashMap<>();

    public PFWorldSpace(World world) {
        this.world = world;
    }

    @Override
    public IBlockObject blockObjectAt(int x, int y, int z) {
        final Block block = world.getBlock(x, y, z);
        return PFBlockObject.getBlockObject(block);
    }

    @Override
    public IColumnarSpace columnarSpaceAt(int cx, int cz) {
        final Chunk chunk = world.getChunk(cx, cz);
        if (chunk == null) {
            return null;
        }

        return chunkSpaceMap.computeIfAbsent(chunk, c -> {
            final PFColumnarSpace cs = new PFColumnarSpace(this, c);
            c.setColumnarSpace(cs);
            return cs;
        });
    }

    public World getWorld() {
        return world;
    }
}
