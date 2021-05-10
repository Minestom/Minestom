package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.SpiderMeta} instead.
 */
@Deprecated
public class EntityCaveSpider extends EntityCreature implements Monster {
    public EntityCaveSpider(Position spawnPosition) {
        super(EntityType.CAVE_SPIDER, spawnPosition);
        setBoundingBox(0.7f, 0.5f, 0.7f);
    }
}
