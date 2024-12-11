package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class GhastMeta extends FlyingMeta {
    public GhastMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return metadata.get(MetadataDef.Ghast.IS_ATTACKING);
    }

    public void setAttacking(boolean value) {
        metadata.set(MetadataDef.Ghast.IS_ATTACKING, value);
    }

}
