package net.minestom.server.entity.metadata.animal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class TurtleMeta extends AnimalMeta {
    public TurtleMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull Point getHomePosition() {
        return metadata.get(MetadataDef.Turtle.HOME_POS);
    }

    public void setBlockPosition(@NotNull Point value) {
        metadata.set(MetadataDef.Turtle.HOME_POS, value);
    }

    public boolean isHasEgg() {
        return metadata.get(MetadataDef.Turtle.HAS_EGG);
    }

    public void setHasEgg(boolean value) {
        metadata.set(MetadataDef.Turtle.HAS_EGG, value);
    }

    public boolean isLayingEgg() {
        return metadata.get(MetadataDef.Turtle.IS_LAYING_EGG);
    }

    public void setLayingEgg(boolean value) {
        metadata.set(MetadataDef.Turtle.IS_LAYING_EGG, value);
    }

    public @NotNull Point getTravelPosition() {
        return metadata.get(MetadataDef.Turtle.TRAVEL_POS);
    }

    public void setTravelPosition(@NotNull Point value) {
        metadata.set(MetadataDef.Turtle.TRAVEL_POS, value);
    }

    public boolean isGoingHome() {
        return metadata.get(MetadataDef.Turtle.IS_GOING_HOME);
    }

    public void setGoingHome(boolean value) {
        metadata.set(MetadataDef.Turtle.IS_GOING_HOME, value);
    }

    public boolean isTravelling() {
        return metadata.get(MetadataDef.Turtle.IS_TRAVELING);
    }

    public void setTravelling(boolean value) {
        metadata.set(MetadataDef.Turtle.IS_TRAVELING, value);
    }

}
