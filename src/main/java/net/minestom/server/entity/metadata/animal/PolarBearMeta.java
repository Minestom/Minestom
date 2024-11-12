package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PolarBearMeta extends AnimalMeta {
    public PolarBearMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isStandingUp() {
        return metadata.get(MetadataDef.PolarBear.IS_STANDING_UP);
    }

    public void setStandingUp(boolean value) {
        metadata.set(MetadataDef.PolarBear.IS_STANDING_UP, value);
    }

}
