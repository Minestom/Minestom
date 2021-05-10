package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends WaterAnimalMeta {

    public DolphinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public BlockPosition getTreasurePosition() {
        return super.metadata.getIndex((byte) 15, new BlockPosition(0, 0, 0));
    }

    public void setTreasurePosition(@NotNull BlockPosition value) {
        super.metadata.setIndex((byte) 15, Metadata.Position(value));
    }

    public boolean isCanFindTreasure() {
        return super.metadata.getIndex((byte) 16, false);
    }

    public void setCanFindTreasure(boolean value) {
        super.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isHasFish() {
        return super.metadata.getIndex((byte) 17, false);
    }

    public void setHasFish(boolean value) {
        super.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

}
