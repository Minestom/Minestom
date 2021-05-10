package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.GiantMeta} instead.
 */
@Deprecated
public class EntityGiant extends EntityCreature implements Monster {
    public EntityGiant(Position spawnPosition) {
        super(EntityType.GIANT, spawnPosition);
        setBoundingBox(3.6f, 10.8f, 3.6f);
    }
}
