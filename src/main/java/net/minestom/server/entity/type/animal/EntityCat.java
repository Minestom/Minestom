package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.TameableAnimal;
import net.minestom.server.utils.Position;

public class EntityCat extends EntityCreature implements TameableAnimal {
    public EntityCat(Position spawnPosition) {
        super(EntityType.CAT, spawnPosition);
        setBoundingBox(0.6f, 0.7f, 0.6f);
    }
}
