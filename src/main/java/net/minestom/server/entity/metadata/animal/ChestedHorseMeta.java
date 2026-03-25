package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public sealed abstract class ChestedHorseMeta extends AbstractHorseMeta permits DonkeyMeta, LlamaMeta, MuleMeta {
    protected ChestedHorseMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasChest() {
        return get(MetadataDef.ChestedHorse.HAS_CHEST);
    }

    public void setHasChest(boolean value) {
        set(MetadataDef.ChestedHorse.HAS_CHEST, value);
    }

}
