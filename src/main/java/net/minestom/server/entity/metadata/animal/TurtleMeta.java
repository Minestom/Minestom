package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class TurtleMeta extends AnimalMeta {

    public TurtleMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public BlockPosition getHomePosition() {
        return getMetadata().getIndex((byte) 16, new BlockPosition(0, 0, 0));
    }

    public void setBlockPosition(@NotNull BlockPosition value) {
        getMetadata().setIndex((byte) 16, Metadata.Position(value));
    }

    public boolean isHasEgg() {
        return getMetadata().getIndex((byte) 17, false);
    }

    public void setHasEgg(boolean value) {
        getMetadata().setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isLayingEgg() {
        return getMetadata().getIndex((byte) 18, false);
    }

    public void setLayingEgg(boolean value) {
        getMetadata().setIndex((byte) 18, Metadata.Boolean(value));
    }

    @NotNull
    public BlockPosition getTravelPosition() {
        return getMetadata().getIndex((byte) 19, new BlockPosition(0, 0, 0));
    }

    public void setTravelPosition(@NotNull BlockPosition value) {
        getMetadata().setIndex((byte) 19, Metadata.Position(value));
    }

    public boolean isGoingHome() {
        return getMetadata().getIndex((byte) 20, false);
    }

    public void setGoingHome(boolean value) {
        getMetadata().setIndex((byte) 20, Metadata.Boolean(value));
    }

    public boolean isTravelling() {
        return getMetadata().getIndex((byte) 21, false);
    }

    public void setTravelling(boolean value) {
        getMetadata().setIndex((byte) 21, Metadata.Boolean(value));
    }

}
