package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.zombie.ZombieMeta} instead.
 */
@Deprecated
public class EntityZombie extends EntityCreature implements Monster {

    public EntityZombie(@NotNull Position spawnPosition) {
        this(EntityType.ZOMBIE, spawnPosition);
    }

    EntityZombie(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }

    public boolean isBaby() {
        return metadata.getIndex((byte) 15, false);
    }

    public void setBaby(boolean baby) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(baby));
    }

    public boolean isBecomingDrowned() {
        return metadata.getIndex((byte) 17, false);
    }

    public void setBecomingDrowned(boolean becomingDrowned) {
        this.metadata.setIndex((byte) 17, Metadata.Boolean(becomingDrowned));
    }

    @Override
    public double getEyeHeight() {
        return isBaby() ? 0.93 : 1.74;
    }
}
