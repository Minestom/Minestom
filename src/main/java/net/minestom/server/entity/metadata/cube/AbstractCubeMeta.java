package net.minestom.server.entity.metadata.cube;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;

public sealed abstract class AbstractCubeMeta extends AgeableMobMeta permits MagmaCubeMeta, SlimeMeta, SulfurCubeMeta {
    protected AbstractCubeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getSize() {
        return metadata.get(sizeComponent());
    }

    public void setSize(int value) {
        this.consumeEntity((entity) -> {
            float boxSize = 0.51000005f * value;
            entity.setBoundingBox(boxSize, boxSize, boxSize);
        });
        metadata.set(sizeComponent(), value);
    }

    protected abstract MetadataDef.Entry<Integer> sizeComponent();
}
