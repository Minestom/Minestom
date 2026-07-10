package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class BlazeMeta extends MonsterMeta {
    public BlazeMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isOnFire() {
        return metadata.get(MetadataDef.Blaze.IS_ON_FIRE);
    }

    public void setOnFire(boolean value) {
        metadata.set(MetadataDef.Blaze.IS_ON_FIRE, value);
    }

}
