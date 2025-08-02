package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class GhastMeta extends FlyingMeta {
    public GhastMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return metadata.get(MetadataDef.Ghast.IS_ATTACKING);
    }

    public void setAttacking(boolean value) {
        metadata.set(MetadataDef.Ghast.IS_ATTACKING, value);
    }

}
