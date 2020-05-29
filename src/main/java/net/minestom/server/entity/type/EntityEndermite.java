package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;

public class EntityEndermite extends EntityCreature {
    public EntityEndermite(Position spawnPosition) {
        super(EntityType.ENDERMITE, spawnPosition);
        setBoundingBox(0.4f, 0.3f, 0.4f);
    }
}
