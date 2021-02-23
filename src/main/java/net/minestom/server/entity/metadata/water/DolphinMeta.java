package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends WaterAnimalMeta {

    public DolphinMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public BlockPosition getTreasurePosition() {
        return getMetadata().getIndex((byte) 15, new BlockPosition(0, 0, 0));
    }

    public void setTreasurePosition(@NotNull BlockPosition value) {
        getMetadata().setIndex((byte) 15, Metadata.Position(value));
    }

    public boolean isCanFindTreasure() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setCanFindTreasure(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isHasFish() {
        return getMetadata().getIndex((byte) 17, false);
    }

    public void setHasFish(boolean value) {
        getMetadata().setIndex((byte) 17, Metadata.Boolean(value));
    }

}
