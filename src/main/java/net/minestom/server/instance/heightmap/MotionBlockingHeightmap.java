package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockTags;
import net.minestom.server.registry.RegistryTag;

public class MotionBlockingHeightmap extends Heightmap {
    private static final RegistryTag<Block> BLOCKS_MOTION_TAG = Block.staticRegistry().getOrCreateTag(BlockTags.BLOCKS_MOTION_IN_HEIGHTMAP);

    public MotionBlockingHeightmap(Chunk attachedChunk) {
        super(attachedChunk);
    }

    @Override
    protected boolean checkBlock(Block block) {
        return block.fluid() || BLOCKS_MOTION_TAG.contains(block.registryKey());
    }

    @Override
    public Type type() {
        return Type.MOTION_BLOCKING;
    }
}
