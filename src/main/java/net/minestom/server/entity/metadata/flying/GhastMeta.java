package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class GhastMeta extends FlyingMeta {
    public GhastMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return get(MetadataDef.Ghast.IS_ATTACKING);
    }

    public void setAttacking(boolean value) {
        set(MetadataDef.Ghast.IS_ATTACKING, value);
    }

}
