package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.minecart.AbstractMinecartMeta;
import net.minestom.server.entity.metadata.other.BoatMeta;

public sealed abstract class AbstractVehicleMeta extends EntityMeta permits AbstractMinecartMeta, BoatMeta {
    protected AbstractVehicleMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getShakingTicks() {
        return get(MetadataDef.AbstractVehicle.SHAKING_POWER);
    }

    public void setShakingTicks(int value) {
        set(MetadataDef.AbstractVehicle.SHAKING_POWER, value);
    }

    public int getShakingDirection() {
        return get(MetadataDef.AbstractVehicle.SHAKING_DIRECTION);
    }

    public void setShakingDirection(int value) {
        set(MetadataDef.AbstractVehicle.SHAKING_DIRECTION, value);
    }

    public float getShakingMultiplier() {
        return get(MetadataDef.AbstractVehicle.SHAKING_MULTIPLIER);
    }

    public void setShakingMultiplier(float value) {
        set(MetadataDef.AbstractVehicle.SHAKING_MULTIPLIER, value);
    }
}
