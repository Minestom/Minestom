package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import com.extollit.gaming.ai.path.model.IBlockDescription;
import com.extollit.gaming.ai.path.model.IColumnarSpace;
import com.extollit.gaming.ai.path.model.IInstanceSpace;
import net.minestom.server.world.Chunk;
import net.minestom.server.block.Block;

public class PFColumnarSpace implements IColumnarSpace {

    private final ColumnarOcclusionFieldList occlusionFieldList = new ColumnarOcclusionFieldList(this);
    private final PFWorldSpace worldSpace;
    private final Chunk chunk;


    public PFColumnarSpace(PFWorldSpace worldSpace, Chunk chunk) {
        this.worldSpace = worldSpace;
        this.chunk = chunk;
    }

    @Override
    public IBlockDescription blockAt(int x, int y, int z) {
        final Block block = chunk.getBlock(x, y, z);
        return PFBlockDescription.getBlockDescription(block);
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
        return worldSpace;
    }
}
