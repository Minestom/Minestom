package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;

public class EntityPigZombie extends EntityCreature {
    public EntityPigZombie(Position spawnPosition) {
        super(EntityType.ZOMBIE_PIGMAN, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }
}
