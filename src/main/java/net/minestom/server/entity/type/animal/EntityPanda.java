package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityPanda extends EntityCreature implements Animal {
    public EntityPanda(Position spawnPosition) {
        super(EntityType.PANDA, spawnPosition);
        setBoundingBox(1.3f, 1.25f, 1.3f);
    }
}
