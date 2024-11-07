package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class BlazeMeta extends MonsterMeta {
    public BlazeMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isOnFire() {
        return metadata.get(MetadataDef.Blaze.IS_ON_FIRE);
    }

    public void setOnFire(boolean value) {
        metadata.set(MetadataDef.Blaze.IS_ON_FIRE, value);
    }

}
