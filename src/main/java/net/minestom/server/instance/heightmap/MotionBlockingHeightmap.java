package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;

public class MotionBlockingHeightmap extends Heightmap {
    public MotionBlockingHeightmap(Chunk attachedChunk) {
        super(attachedChunk);
    }

    @Override
    protected boolean checkBlock(Block block) {
        return block.blocksMotion() || block.fluid();
    }

    @Override
    public Type type() {
        return Type.MOTION_BLOCKING;
    }
}
