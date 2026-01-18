package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.water.WaterAnimalMeta;

public sealed class AbstractFishMeta extends WaterAnimalMeta permits CodMeta, PufferfishMeta, SalmonMeta, TadpoleMeta, TropicalFishMeta {
    protected AbstractFishMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isFromBucket() {
        return metadata.get(MetadataDef.AbstractFish.FROM_BUCKET);
    }

    public void setFromBucket(boolean value) {
        metadata.set(MetadataDef.AbstractFish.FROM_BUCKET, value);
    }
}
