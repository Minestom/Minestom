package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class PillagerMeta extends AbstractIllagerMeta {
    public PillagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isChargingCrossbow() {
        return get(MetadataDef.Pillager.IS_CHARGING);
    }

    public void setChargingCrossbow(boolean value) {
        set(MetadataDef.Pillager.IS_CHARGING, value);
    }

}
