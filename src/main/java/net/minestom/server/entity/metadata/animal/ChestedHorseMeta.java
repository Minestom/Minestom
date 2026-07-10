package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class ChestedHorseMeta extends AbstractHorseMeta {
    protected ChestedHorseMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasChest() {
        return metadata.get(MetadataDef.ChestedHorse.HAS_CHEST);
    }

    public void setHasChest(boolean value) {
        metadata.set(MetadataDef.ChestedHorse.HAS_CHEST, value);
    }

}
