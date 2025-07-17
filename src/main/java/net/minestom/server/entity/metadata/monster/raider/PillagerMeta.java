package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PillagerMeta extends AbstractIllagerMeta {
    public PillagerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isChargingCrossbow() {
        return metadata.get(MetadataDef.Pillager.IS_CHARGING);
    }

    public void setChargingCrossbow(boolean value) {
        metadata.set(MetadataDef.Pillager.IS_CHARGING, value);
    }

}
