package net.minestom.server.entity.metadata.monster.skeleton;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class BoggedMeta extends AbstractSkeletonMeta {
    public BoggedMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Bogged.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Bogged.IS_SHEARED, value);
    }
}
