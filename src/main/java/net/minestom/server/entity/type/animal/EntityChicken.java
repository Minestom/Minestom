package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityChicken extends EntityCreature implements Animal {
    public EntityChicken(Position spawnPosition) {
        super(EntityType.CHICKEN, spawnPosition);
        setBoundingBox(0.4f, 0.7f, 0.4f);
    }
}
