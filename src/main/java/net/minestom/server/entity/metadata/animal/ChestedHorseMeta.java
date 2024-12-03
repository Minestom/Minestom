package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class ChestedHorseMeta extends AbstractHorseMeta {
    protected ChestedHorseMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasChest() {
        return metadata.get(MetadataDef.ChestedHorse.HAS_CHEST);
    }

    public void setHasChest(boolean value) {
        metadata.set(MetadataDef.ChestedHorse.HAS_CHEST, value);
    }

}
