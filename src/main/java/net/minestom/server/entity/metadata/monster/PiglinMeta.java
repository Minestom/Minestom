package net.minestom.server.entity.metadata.monster;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class PiglinMeta extends BasePiglinMeta {
    public PiglinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return get(MetadataDef.Piglin.IS_BABY);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getBoundingBox();
            if (value) {
                double width = bb.width() / 2;
                entity.setBoundingBox(width, bb.height() / 2, width);
            } else {
                double width = bb.width() * 2;
                entity.setBoundingBox(width, bb.height() * 2, width);
            }
        });
        set(MetadataDef.Piglin.IS_BABY, value);
    }

    public boolean isChargingCrossbow() {
        return get(MetadataDef.Piglin.IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean value) {
        set(MetadataDef.Piglin.IS_CHARGING_CROSSBOW, value);
    }

    public boolean isDancing() {
        return get(MetadataDef.Piglin.IS_DANCING);
    }

    public void setDancing(boolean value) {
        set(MetadataDef.Piglin.IS_DANCING, value);
    }

}
