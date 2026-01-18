package net.minestom.server.entity.metadata.water;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class DolphinMeta extends AgeableWaterAnimalMeta {
    public DolphinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Point getTreasurePosition() {
        return get(MetadataDef.Dolphin.TREASURE_POSITION);
    }

    public void setTreasurePosition(Point value) {
        set(MetadataDef.Dolphin.TREASURE_POSITION, value);
    }

    public boolean isHasFish() {
        return get(MetadataDef.Dolphin.HAS_FISH);
    }

    public void setHasFish(boolean value) {
        set(MetadataDef.Dolphin.HAS_FISH, value);
    }

    public int getMoistureLevel() {
        return get(MetadataDef.Dolphin.MOISTURE_LEVEL);
    }

    public void setMoistureLevel(int value) {
        set(MetadataDef.Dolphin.MOISTURE_LEVEL, value);
    }
}
