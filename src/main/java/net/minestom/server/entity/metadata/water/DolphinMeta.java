package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends WaterAnimalMeta {
    public static final byte OFFSET = WaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public DolphinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public BlockPosition getTreasurePosition() {
        return super.metadata.getIndex(OFFSET, new BlockPosition(0, 0, 0));
    }

    public void setTreasurePosition(@NotNull BlockPosition value) {
        super.metadata.setIndex(OFFSET, Metadata.Position(value));
    }

    public boolean isCanFindTreasure() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setCanFindTreasure(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isHasFish() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setHasFish(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

}
