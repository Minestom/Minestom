package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityLlama extends EntityCreature implements Animal {
    public EntityLlama(Position spawnPosition) {
        super(EntityType.LLAMA, spawnPosition);
        setBoundingBox(0.45f, 0.9375f, 0.45f);
    }
}
