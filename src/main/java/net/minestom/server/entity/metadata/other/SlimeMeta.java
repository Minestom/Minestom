package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;

public sealed class SlimeMeta extends MobMeta permits MagmaCubeMeta {
    public SlimeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getSize() {
        return get(MetadataDef.Slime.SIZE);
    }

    public void setSize(int value) {
        this.consumeEntity((entity) -> {
            float boxSize = 0.51000005f * value;
            entity.setBoundingBox(boxSize, boxSize, boxSize);
        });
        set(MetadataDef.Slime.SIZE, value);
    }

}
