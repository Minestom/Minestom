package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;

public final class BoatMeta extends AbstractVehicleMeta {
    public BoatMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isLeftPaddleTurning() {
        return get(MetadataDef.Boat.IS_LEFT_PADDLE_TURNING);
    }

    public void setLeftPaddleTurning(boolean value) {
        set(MetadataDef.Boat.IS_LEFT_PADDLE_TURNING, value);
    }

    public boolean isRightPaddleTurning() {
        return get(MetadataDef.Boat.IS_RIGHT_PADDLE_TURNING);
    }

    public void setRightPaddleTurning(boolean value) {
        set(MetadataDef.Boat.IS_RIGHT_PADDLE_TURNING, value);
    }

    public int getSplashTimer() {
        return get(MetadataDef.Boat.SPLASH_TIMER);
    }

    public void setSplashTimer(int value) {
        set(MetadataDef.Boat.SPLASH_TIMER, value);
    }
}
