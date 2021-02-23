package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityEnderman extends EntityCreature implements Monster {

    public EntityEnderman(@NotNull Position spawnPosition) {
        super(EntityType.ENDERMAN, spawnPosition);
        setBoundingBox(.6D, 2.9D, .6D);
    }

    public EntityEnderman(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.ENDERMAN, spawnPosition, instance);
        setBoundingBox(.6D, 2.9D, .6D);
    }

    public Integer getCarriedBlockID() {
        return this.metadata.getIndex((byte) 15, null);
    }

    public void setCarriedBlockID(@Nullable Integer value) {
        this.metadata.setIndex((byte) 15, Metadata.OptBlockID(value));
    }

    public boolean isScreaming() {
        return this.metadata.getIndex((byte) 16, false);
    }

    public void setScreaming(boolean value) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isStaring() {
        return this.metadata.getIndex((byte) 17, false);
    }

    public void setStaring(boolean value) {
        this.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

}
