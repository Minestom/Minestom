package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class HappyGhastMeta extends AnimalMeta {

    public HappyGhastMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isLeashHolder() {
        return get(MetadataDef.HappyGhast.IS_LEASH_HOLDER);
    }

    public void setLeashHolder(boolean value) {
        set(MetadataDef.HappyGhast.IS_LEASH_HOLDER, value);
    }

    public boolean isStaysStill() {
        return get(MetadataDef.HappyGhast.STAYS_STILL);
    }

    public void setStaysStill(boolean value) {
        set(MetadataDef.HappyGhast.STAYS_STILL, value);
    }
}
