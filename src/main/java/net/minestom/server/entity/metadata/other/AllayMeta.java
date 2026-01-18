package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;

public final class AllayMeta extends PathfinderMobMeta {
    public AllayMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDancing() {
        return get(MetadataDef.Allay.IS_DANCING);
    }

    public void setDancing(boolean value) {
        set(MetadataDef.Allay.IS_DANCING, value);
    }

    public boolean canDuplicate() {
        return get(MetadataDef.Allay.CAN_DUPLICATE);
    }

    public void setCanDuplicate(boolean value) {
        set(MetadataDef.Allay.CAN_DUPLICATE, value);
    }

}
