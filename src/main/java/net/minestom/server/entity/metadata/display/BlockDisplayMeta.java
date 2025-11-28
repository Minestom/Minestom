package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.instance.block.Block;

public class BlockDisplayMeta extends AbstractDisplayMeta {
    public BlockDisplayMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Block getBlockStateId() {
        return metadata.get(MetadataDef.BlockDisplay.DISPLAYED_BLOCK_STATE);
    }

    public void setBlockState(Block value) {
        metadata.set(MetadataDef.BlockDisplay.DISPLAYED_BLOCK_STATE, value);
    }
}
