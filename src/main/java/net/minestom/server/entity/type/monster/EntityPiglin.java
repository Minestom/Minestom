package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityPiglin extends EntityBasePiglin {

    public EntityPiglin(@NotNull Position spawnPosition) {
        super(EntityType.PIGLIN, spawnPosition);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public EntityPiglin(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.PIGLIN, spawnPosition, instance);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public boolean isBaby() {
        return this.metadata.getIndex((byte) 16, false);
    }

    public void setBaby(boolean value) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isChargingCrossbow() {
        return this.metadata.getIndex((byte) 17, false);
    }

    public void setChargingCrossbow(boolean value) {
        this.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isDancing() {
        return this.metadata.getIndex((byte) 18, false);
    }

    public void setDancing(boolean value) {
        this.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

    @Override
    public double getEyeHeight() {
        return isBaby() ? super.getEyeHeight() / 2 : super.getEyeHeight();
    }

}
