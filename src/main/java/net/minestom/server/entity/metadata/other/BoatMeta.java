package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;
import org.jetbrains.annotations.NotNull;

public class BoatMeta extends AbstractVehicleMeta {
    public BoatMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[metadata.get(MetadataDef.Boat.TYPE)];
    }

    public void setType(@NotNull Type value) {
        metadata.set(MetadataDef.Boat.TYPE, value.ordinal());
    }

    public boolean isLeftPaddleTurning() {
        return metadata.get(MetadataDef.Boat.IS_LEFT_PADDLE_TURNING);
    }

    public void setLeftPaddleTurning(boolean value) {
        metadata.set(MetadataDef.Boat.IS_LEFT_PADDLE_TURNING, value);
    }

    public boolean isRightPaddleTurning() {
        return metadata.get(MetadataDef.Boat.IS_RIGHT_PADDLE_TURNING);
    }

    public void setRightPaddleTurning(boolean value) {
        metadata.set(MetadataDef.Boat.IS_RIGHT_PADDLE_TURNING, value);
    }

    public int getSplashTimer() {
        return metadata.get(MetadataDef.Boat.SPLASH_TIMER);
    }

    public void setSplashTimer(int value) {
        metadata.set(MetadataDef.Boat.SPLASH_TIMER, value);
    }

    public enum Type {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        CHERRY,
        DARK_OAK,
        MANGROVE,
        BAMBOO;

        private final static Type[] VALUES = values();
    }

}
