package net.minestom.server.entity.metadata.water;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends AgeableWaterAnimalMeta {
    public static final byte OFFSET = AgeableWaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public DolphinMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Point getTreasurePosition() {
        return super.metadata.getIndex(OFFSET, Vec.ZERO);
    }

    public void setTreasurePosition(@NotNull Point value) {
        super.metadata.setIndex(OFFSET, Metadata.BlockPosition(value));
    }

    public boolean isHasFish() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setHasFish(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public int getMoistureLevel() {
        return super.metadata.getIndex(OFFSET + 2, 2400);
    }

    public void setMoistureLevel(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
    }
}
