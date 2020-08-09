package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityFox extends EntityCreature implements Animal {
    public EntityFox(Position spawnPosition) {
        super(EntityType.FOX, spawnPosition);
        setBoundingBox(0.6f, 0.7f, 0.6f);
    }
}
