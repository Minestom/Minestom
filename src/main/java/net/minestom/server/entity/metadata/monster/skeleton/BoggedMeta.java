package net.minestom.server.entity.metadata.monster.skeleton;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class BoggedMeta extends AbstractSkeletonMeta {
    public BoggedMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Bogged.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Bogged.IS_SHEARED, value);
    }
}
