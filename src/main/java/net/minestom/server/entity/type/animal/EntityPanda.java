package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.animal.PandaMeta} instead.
 */
@Deprecated
public class EntityPanda extends AgeableCreature implements Animal {
    public EntityPanda(Position spawnPosition) {
        super(EntityType.PANDA, spawnPosition);
        setBoundingBox(1.3f, 1.25f, 1.3f);
    }
}
