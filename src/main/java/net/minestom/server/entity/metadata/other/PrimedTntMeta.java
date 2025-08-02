package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.block.Block;

public class PrimedTntMeta extends EntityMeta {
    public PrimedTntMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getFuseTime() {
        return metadata.get(MetadataDef.PrimedTnt.FUSE_TIME);
    }

    public void setFuseTime(int value) {
        metadata.set(MetadataDef.PrimedTnt.FUSE_TIME, value);
    }

    public Block getBlockState() {
        return metadata.get(MetadataDef.PrimedTnt.BLOCK_STATE);
    }

    public void setBlockState(Block block) {
        metadata.set(MetadataDef.PrimedTnt.BLOCK_STATE, block);
    }

}
