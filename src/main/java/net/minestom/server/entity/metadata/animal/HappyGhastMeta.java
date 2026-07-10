package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class HappyGhastMeta extends AnimalMeta {

    public HappyGhastMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isLeashHolder() {
        return metadata.get(MetadataDef.HappyGhast.IS_LEASH_HOLDER);
    }

    public void setLeashHolder(boolean value) {
        metadata.set(MetadataDef.HappyGhast.IS_LEASH_HOLDER, value);
    }

    public boolean isStaysStill() {
        return metadata.get(MetadataDef.HappyGhast.STAYS_STILL);
    }

    public void setStaysStill(boolean value) {
        metadata.set(MetadataDef.HappyGhast.STAYS_STILL, value);
    }
}
