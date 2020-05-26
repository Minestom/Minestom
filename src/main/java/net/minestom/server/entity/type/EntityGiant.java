package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;

public class EntityGiant extends EntityCreature {
    public EntityGiant(Position spawnPosition) {
        super(EntityType.GIANT, spawnPosition);
    }
}
