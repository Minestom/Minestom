package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class BlazeMeta extends MonsterMeta {
    public BlazeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isOnFire() {
        return get(MetadataDef.Blaze.IS_ON_FIRE);
    }

    public void setOnFire(boolean value) {
        set(MetadataDef.Blaze.IS_ON_FIRE, value);
    }

}
