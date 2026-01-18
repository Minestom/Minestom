package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.minecart.AbstractMinecartMeta;
import net.minestom.server.entity.metadata.other.BoatMeta;

public sealed class AbstractVehicleMeta extends EntityMeta permits AbstractMinecartMeta, BoatMeta {
    public AbstractVehicleMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getShakingTicks() {
        return metadata.get(MetadataDef.AbstractVehicle.SHAKING_POWER);
    }

    public void setShakingTicks(int value) {
        metadata.set(MetadataDef.AbstractVehicle.SHAKING_POWER, value);
    }

    public int getShakingDirection() {
        return metadata.get(MetadataDef.AbstractVehicle.SHAKING_DIRECTION);
    }

    public void setShakingDirection(int value) {
        metadata.set(MetadataDef.AbstractVehicle.SHAKING_DIRECTION, value);
    }

    public float getShakingMultiplier() {
        return metadata.get(MetadataDef.AbstractVehicle.SHAKING_MULTIPLIER);
    }

    public void setShakingMultiplier(float value) {
        metadata.set(MetadataDef.AbstractVehicle.SHAKING_MULTIPLIER, value);
    }
}
