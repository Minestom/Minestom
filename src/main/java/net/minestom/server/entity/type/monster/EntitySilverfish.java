package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.SilverfishMeta} instead.
 */
@Deprecated
public class EntitySilverfish extends EntityCreature implements Monster {
    public EntitySilverfish(Position spawnPosition) {
        super(EntityType.SILVERFISH, spawnPosition);
        setBoundingBox(0.4f, 0.3f, 0.4f);
    }
}
