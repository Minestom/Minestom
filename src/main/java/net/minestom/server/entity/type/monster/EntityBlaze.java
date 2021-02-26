package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.BlazeMeta} instead.
 */
@Deprecated
public class EntityBlaze extends EntityCreature implements Monster {

    public EntityBlaze(Position spawnPosition) {
        super(EntityType.BLAZE, spawnPosition);
        setBoundingBox(0.6f, 1.8f, 0.6f);
    }

    @Override
    public void setOnFire(boolean fire) {
        super.setOnFire(fire);
        this.metadata.setIndex((byte) 15, Metadata.Byte((byte) (fire ? 1 : 0)));
    }
}
