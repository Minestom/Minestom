package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityStrider extends AgeableCreature implements Animal {

    public EntityStrider(@NotNull Position spawnPosition) {
        super(EntityType.STRIDER, spawnPosition);
        setBoundingBox(.9D, 1.7D, .9D);
    }

    public EntityStrider(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.STRIDER, spawnPosition, instance);
        setBoundingBox(.9D, 1.7D, .9D);
    }

    public int getTimeToBoost() {
        return this.metadata.getIndex((byte) 16, 0);
    }

    public void setTimeToBoost(int value) {
        this.metadata.setIndex((byte) 16, Metadata.VarInt(value));
    }

    public boolean isShaking() {
        return this.metadata.getIndex((byte) 17, false);
    }

    public void setShaking(boolean value) {
        this.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isHasSaddle() {
        return this.metadata.getIndex((byte) 18, false);
    }

    public void setHasSaddle(boolean value) {
        this.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

}
