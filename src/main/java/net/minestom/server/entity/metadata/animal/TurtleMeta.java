package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class TurtleMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 6;

    public TurtleMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public BlockPosition getHomePosition() {
        return super.metadata.getIndex(OFFSET, new BlockPosition(0, 0, 0));
    }

    public void setBlockPosition(@NotNull BlockPosition value) {
        super.metadata.setIndex(OFFSET, Metadata.Position(value));
    }

    public boolean isHasEgg() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setHasEgg(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isLayingEgg() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setLayingEgg(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

    @NotNull
    public BlockPosition getTravelPosition() {
        return super.metadata.getIndex(OFFSET + 3, new BlockPosition(0, 0, 0));
    }

    public void setTravelPosition(@NotNull BlockPosition value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Position(value));
    }

    public boolean isGoingHome() {
        return super.metadata.getIndex(OFFSET + 4, false);
    }

    public void setGoingHome(boolean value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Boolean(value));
    }

    public boolean isTravelling() {
        return super.metadata.getIndex(OFFSET + 5, false);
    }

    public void setTravelling(boolean value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.Boolean(value));
    }

}
