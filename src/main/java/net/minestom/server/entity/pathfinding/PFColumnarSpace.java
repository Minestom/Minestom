package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import com.extollit.gaming.ai.path.model.IBlockDescription;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PFColumnarSpace implements IColumnarSpace {
    private final ColumnarOcclusionFieldList occlusionFieldList = new ColumnarOcclusionFieldList(this);
    private final PFInstanceSpace instanceSpace;
    private final Chunk chunk;

    PFColumnarSpace(PFInstanceSpace instanceSpace, Chunk chunk) {
        this.instanceSpace = instanceSpace;
        this.chunk = chunk;
    }

    @Override
    public IBlockDescription blockAt(int x, int y, int z) {
        final Block block = chunk.getBlock(x, y, z);
        return PFBlock.get(block);
    }

    @Override
    public int metaDataAt(int x, int y, int z) {
        return 0;
    }

    @Override
    public ColumnarOcclusionFieldList occlusionFields() {
        return occlusionFieldList;
    }

    @Override
    public IInstanceSpace instance() {
        return instanceSpace;
    }
}
