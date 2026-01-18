package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.water.WaterAnimalMeta;

public sealed abstract class AbstractFishMeta extends WaterAnimalMeta permits CodMeta, PufferfishMeta, SalmonMeta, TadpoleMeta, TropicalFishMeta {
    protected AbstractFishMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isFromBucket() {
        return get(MetadataDef.AbstractFish.FROM_BUCKET);
    }

    public void setFromBucket(boolean value) {
        set(MetadataDef.AbstractFish.FROM_BUCKET, value);
    }
}
