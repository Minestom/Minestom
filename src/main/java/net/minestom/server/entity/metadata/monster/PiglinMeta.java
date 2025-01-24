package net.minestom.server.entity.metadata.monster;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PiglinMeta extends BasePiglinMeta {
    public PiglinMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return metadata.get(MetadataDef.Piglin.IS_BABY);
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
        metadata.set(MetadataDef.Piglin.IS_BABY, value);
    }

    public boolean isChargingCrossbow() {
        return metadata.get(MetadataDef.Piglin.IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean value) {
        metadata.set(MetadataDef.Piglin.IS_CHARGING_CROSSBOW, value);
    }

    public boolean isDancing() {
        return metadata.get(MetadataDef.Piglin.IS_DANCING);
    }

    public void setDancing(boolean value) {
        metadata.set(MetadataDef.Piglin.IS_DANCING, value);
    }

}
