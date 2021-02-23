package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.TameableAnimalCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityWolf extends TameableAnimalCreature {

    public EntityWolf(@NotNull Position spawnPosition) {
        super(EntityType.WOLF, spawnPosition);
        setBoundingBox(.6D, .85D, .6D);
    }

    public EntityWolf(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.WOLF, spawnPosition, instance);
        setBoundingBox(.6D, .85D, .6D);
    }

    public boolean isBegging() {
        return this.metadata.getIndex((byte) 18, false);
    }

    public void setBegging(boolean value) {
        this.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return this.metadata.getIndex((byte) 19, 14);
    }

    public void setCollarColor(int value) {
        this.metadata.setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getAngerTime() {
        return this.metadata.getIndex((byte) 20, 0);
    }

    public void setAngerTime(int value) {
        this.metadata.setIndex((byte) 20, Metadata.VarInt(value));
    }

}
