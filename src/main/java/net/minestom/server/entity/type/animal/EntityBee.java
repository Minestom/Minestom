package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityBee extends EntityCreature implements Animal {
    public EntityBee(Position spawnPosition) {
        super(EntityType.BEE, spawnPosition);
        setBoundingBox(0.7f,0.6f,0.7f);
    }
}
