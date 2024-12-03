package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.NotNull;

public class AllayMeta extends PathfinderMobMeta {
    public AllayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDancing() {
        return metadata.get(MetadataDef.Allay.IS_DANCING);
    }

    public void setDancing(boolean value) {
        metadata.set(MetadataDef.Allay.IS_DANCING, value);
    }

    public boolean canDuplicate() {
        return metadata.get(MetadataDef.Allay.CAN_DUPLICATE);
    }

    public void setCanDuplicate(boolean value) {
        metadata.set(MetadataDef.Allay.CAN_DUPLICATE, value);
    }

}
