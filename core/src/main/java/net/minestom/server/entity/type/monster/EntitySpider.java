package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.SpiderMeta} instead.
 */
@Deprecated
public class EntitySpider extends EntityCreature implements Monster {

    public EntitySpider(Position spawnPosition) {
        super(EntityType.SPIDER, spawnPosition);
        setBoundingBox(1.4f, 0.9f, 1.4f);
    }

    /**
     * Gets if the spider is climbing.
     *
     * @return true if the spider is climbing, false otherwise
     */
    public boolean isClimbing() {
        return metadata.getIndex((byte) 15, false);
    }

    /**
     * Makes the spider climbs.
     *
     * @param climbing true to make the spider climbs, false otherwise
     */
    public void setClimbing(boolean climbing) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(climbing));
    }
}
