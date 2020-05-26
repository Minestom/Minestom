package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;

public class EntityChicken extends EntityCreature {
    public EntityChicken(Position spawnPosition) {
        super(EntityType.CHICKEN, spawnPosition);
    }
}
