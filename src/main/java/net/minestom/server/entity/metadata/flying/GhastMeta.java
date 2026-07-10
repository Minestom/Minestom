package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class GhastMeta extends FlyingMeta {
    public GhastMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return metadata.get(MetadataDef.Ghast.IS_ATTACKING);
    }

    public void setAttacking(boolean value) {
        metadata.set(MetadataDef.Ghast.IS_ATTACKING, value);
    }

}
