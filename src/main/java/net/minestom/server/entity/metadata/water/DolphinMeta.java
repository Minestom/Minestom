package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class DolphinMeta extends AgeableWaterAnimalMeta {
    public DolphinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasFish() {
        return metadata.get(MetadataDef.Dolphin.HAS_FISH);
    }

    public void setHasFish(boolean value) {
        metadata.set(MetadataDef.Dolphin.HAS_FISH, value);
    }

    public int getMoistureLevel() {
        return metadata.get(MetadataDef.Dolphin.MOISTURE_LEVEL);
    }

    public void setMoistureLevel(int value) {
        metadata.set(MetadataDef.Dolphin.MOISTURE_LEVEL, value);
    }
}
