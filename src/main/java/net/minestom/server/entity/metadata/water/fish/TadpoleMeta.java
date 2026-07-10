package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class TadpoleMeta extends AbstractFishMeta {
    public TadpoleMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }


    public boolean isAgeLocked() {
        return metadata.get(MetadataDef.Tadpole.AGE_LOCKED);
    }

    public void setAgeLocked(boolean value) {
        metadata.set(MetadataDef.Tadpole.AGE_LOCKED, value);
    }
}
