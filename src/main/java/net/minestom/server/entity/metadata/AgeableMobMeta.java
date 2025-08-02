package net.minestom.server.entity.metadata;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class AgeableMobMeta extends PathfinderMobMeta {
    protected AgeableMobMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return metadata.get(MetadataDef.AgeableMob.IS_BABY);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getBoundingBox();
            if (value) {
                double width = bb.width() / 2;
                entity.setBoundingBox(width, bb.height() / 2, width);
            } else {
                double width = bb.width() * 2;
                entity.setBoundingBox(width, bb.height() * 2, width);
            }
        });
        metadata.set(MetadataDef.AgeableMob.IS_BABY, value);
    }

}
