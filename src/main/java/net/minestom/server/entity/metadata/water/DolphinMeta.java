package net.minestom.server.entity.metadata.water;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends AgeableWaterAnimalMeta {
    public DolphinMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Point getTreasurePosition() {
        return metadata.get(MetadataDef.Dolphin.TREASURE_POSITION);
    }

    public void setTreasurePosition(@NotNull Point value) {
        metadata.set(MetadataDef.Dolphin.TREASURE_POSITION, value);
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
