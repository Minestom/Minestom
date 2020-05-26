package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;

public class EntityCow extends EntityCreature {
    public EntityCow(Position spawnPosition) {
        super(EntityType.COW, spawnPosition);
    }
}
