package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.monster.EndermiteMeta;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link EndermiteMeta} instead.
 */
@Deprecated
public class EntityEndermite extends EntityCreature implements Monster {
    public EntityEndermite(Position spawnPosition) {
        super(EntityType.ENDERMITE, spawnPosition);
        setBoundingBox(0.4f, 0.3f, 0.4f);
    }
}
