package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityOcelot extends EntityCreature implements Animal {
    public EntityOcelot(Position spawnPosition) {
        super(EntityType.OCELOT, spawnPosition);
        setBoundingBox(0.6f, 0.7f, 0.6f);
    }
}
